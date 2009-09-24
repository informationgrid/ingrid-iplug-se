/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.admin.security;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.security.NutchGuiPrincipal.KnownPrincipal;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SSORealm;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.util.Credential;

public class NutchGuiRealm implements UserRealm, SSORealm {

  class SSOToken {
    private final Principal _principal;
    private final Credential _credential;

    public SSOToken(Principal principal, Credential credential) {
      _principal = principal;
      _credential = credential;
    }

    @Override
    public String toString() {
      return _principal.getName();
    }
  }

  private final Log LOG = LogFactory.getLog(NutchGuiRealm.class);
  private Map<String, SSOToken> _ssoMap = new HashMap<String, SSOToken>();
  private final boolean _securityEnabled;

  public NutchGuiRealm(boolean securityEnabled) {
    _securityEnabled = securityEnabled;
    System.setProperty("java.security.auth.login.config", System
            .getProperty("user.dir")
            + "/conf/nutchgui.auth");
  }

  @Override
  public Principal authenticate(String userName, Object password,
          HttpRequest request) {

    Principal principal = new NutchGuiPrincipal.SuperAdmin("Admin");
    if (_securityEnabled) {
      principal = null;
      try {
        JUserJPasswordCallbackHandler handler = new JUserJPasswordCallbackHandler(
                request);
        LoginContext loginContext = new LoginContext("PropertyFileLogin",
                handler);
        loginContext.login();
        Subject subject = loginContext.getSubject();
        Set<Principal> principals = subject.getPrincipals();
        Principal tmpPrincipal = principals.isEmpty() ? principal : principals
                .iterator().next();
        if (tmpPrincipal instanceof KnownPrincipal) {
          KnownPrincipal knownPrincipal = (KnownPrincipal) tmpPrincipal;
          knownPrincipal.setLoginContext(loginContext);
          principal = knownPrincipal;
          LOG.info("principal has logged in: " + principal);
        }
      } catch (LoginException e) {
        LOG.error("login error for user: " + userName);
      }
    }
    if (principal == null) {
      LOG.info("login failed for userName: " + userName);
    }
    return principal;
  }

  @Override
  public void disassociate(Principal principal) {
    // nothing todo
  }

  @Override
  public String getName() {
    return NutchGuiRealm.class.getSimpleName();
  }

  @Override
  public Principal getPrincipal(String name) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isUserInRole(Principal principal, String role) {
    boolean bit = false;
    if (principal instanceof KnownPrincipal) {
      KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
      bit = knownPrincipal.isInRole(role);
    }
    return bit;
  }

  @Override
  public void logout(Principal principal) {
    try {
      if (principal instanceof KnownPrincipal) {
        KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
        LoginContext loginContext = knownPrincipal.getLoginContext();
        if (loginContext != null) {
          loginContext.logout();
        }
        LOG.info("principal has logged out: " + knownPrincipal);
      }
    } catch (LoginException e) {
      LOG.warn("logout failed", e);
    }
  }

  @Override
  public Principal popRole(Principal principal) {
    // not necessary
    return principal;
  }

  @Override
  public Principal pushRole(Principal principal, String role) {
    // not necessary
    return principal;
  }

  @Override
  public boolean reauthenticate(Principal principal) {
    return (principal instanceof KnownPrincipal);
  }

  @Override
  public void clearSingleSignOn(String userName) {
    Iterator<String> iterator = _ssoMap.keySet().iterator();
    while (iterator.hasNext()) {
      String id = (String) iterator.next();
      SSOToken ssoToken = _ssoMap.get(id);
      if (ssoToken._principal.getName().equals(userName)) {
        LOG.info("remove sso token for id: " + id + " and user name:"
                + userName);
        iterator.remove();
      }
    }
    LOG.info("sso tokens in memory: " + _ssoMap);
  }

  @Override
  public Credential getSingleSignOn(HttpRequest request, HttpResponse response) {
    Credential credential = null;
    String id = generateId(request);
    LOG.info("try to load sso token with id: " + id);
    if (_ssoMap.containsKey(id)) {
      SSOToken ssoToken = _ssoMap.get(id);
      Principal principal = ssoToken._principal;
      LOG.info("found principal: " + principal);
      if (response.getHttpContext().getRealm().reauthenticate(principal)) {
        request.setUserPrincipal(principal);
        request.setAuthUser(principal.getName());
        credential = ssoToken._credential;
      } else {
        _ssoMap.remove(id);
      }
    }
    LOG.info("found credential for id: " + id);
    return credential;
  }

  @Override
  public void setSingleSignOn(HttpRequest request, HttpResponse response,
          Principal principal, Credential credential) {
    Set<String> keySet = _ssoMap.keySet();
    Iterator<String> iterator = keySet.iterator();
    while (iterator.hasNext()) {
      String id = (String) iterator.next();
      SSOToken ssoToken = _ssoMap.get(id);
      if (ssoToken._principal.getName().equals(principal.getName())) {
        LOG.info("remove sso token [" + id + "] for user ["
                + principal.getName() + "].");
        iterator.remove();
      }
    }
    String id = generateId(request);
    LOG.info("create new sso token for id: " + id);
    _ssoMap.put(id, new SSOToken(principal, credential));
    LOG.info("sso tokens in memory: " + _ssoMap);
  }

  private String generateId(HttpRequest request) {
    ServletHttpRequest servletHttpRequest = (ServletHttpRequest) request
            .getWrapper();
    return servletHttpRequest.getSession().getId();
  }
}
