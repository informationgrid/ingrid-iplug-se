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
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractLoginModule implements LoginModule {

  private final Log LOG = LogFactory.getLog(AbstractLoginModule.class);

  private Subject _subject;
  private CallbackHandler _callbackHandler;
  private boolean _authenticated;

  private NutchGuiPrincipal _currentPrincipal;

  private boolean _committed;

  @Override
  public boolean abort() throws LoginException {
    _currentPrincipal = null;
    return (isAuthenticated() && isCommitted());
  }

  @Override
  public boolean commit() throws LoginException {
    if (!isAuthenticated()) {
      _currentPrincipal = null;
      setCommitted(false);
    } else {
      Set<Principal> principals = _subject.getPrincipals();
      principals.add(_currentPrincipal);
      setCommitted(true);
    }
    return isCommitted();
  }

  private void setCommitted(boolean committed) {
    _committed = committed;
  }

  private boolean isCommitted() {
    return _committed;
  }

  @Override
  public void initialize(Subject subject, CallbackHandler callbackHandler,
          Map<String, ?> sharedState, Map<String, ?> options) {
    _subject = subject;
    _callbackHandler = callbackHandler;
  }

  @Override
  public boolean login() throws LoginException {
    NameCallback nameCallback = new NameCallback("user name:");
    PasswordCallback passwordCallback = new PasswordCallback("password:", false);
    try {
      _callbackHandler
              .handle(new Callback[] { nameCallback, passwordCallback });
      String name = nameCallback.getName();
      char[] password = passwordCallback.getPassword();

      if (name != null && name.length() > 0) {
        if (password != null && password.length > 0) {
          NutchGuiPrincipal nutchGuiPrincipal = authenticate(name, new String(
                  password));
          if (nutchGuiPrincipal.isAuthenticated()) {
            setAuthenticated(true);
            _currentPrincipal = nutchGuiPrincipal;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("login failed.", e);
      throw new LoginException(e.getMessage());
    }
    return isAuthenticated();
  }

  private boolean isAuthenticated() {
    return _authenticated;
  }

  private void setAuthenticated(boolean authenticated) {
    _authenticated = authenticated;
  }

  @Override
  public boolean logout() throws LoginException {
    Set<Principal> principals = _subject.getPrincipals();
    principals.remove(_currentPrincipal);
    return true;
  }

  protected abstract NutchGuiPrincipal authenticate(String userName,
          String password);

}
