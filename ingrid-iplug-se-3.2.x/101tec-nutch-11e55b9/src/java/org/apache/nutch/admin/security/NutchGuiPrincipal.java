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
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginContext;

public abstract class NutchGuiPrincipal implements Principal {

  private final Set<String> _roles;
  private final String _name;
  private final String _password;

  public NutchGuiPrincipal(String name, String password, Set<String> roles) {
    _name = name;
    _password = password;
    _roles = roles;
  }

  public Set<String> getRoles() {
    return _roles;
  }

  public String getPassword() {
    return _password;
  }

  @Override
  public String getName() {
    return _name;
  }

  public boolean isInRole(String role) {
    return _roles.contains(role);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    NutchGuiPrincipal other = (NutchGuiPrincipal) obj;
    return other._name.equals(_name);
  }

  @Override
  public String toString() {
    return _name;
  }

  public abstract boolean isAuthenticated();

  public static class KnownPrincipal extends NutchGuiPrincipal {

    private LoginContext _loginContext;

    public KnownPrincipal(String name, String password, Set<String> roles) {
      super(name, password, roles);
    }

    public LoginContext getLoginContext() {
      return _loginContext;
    }

    public void setLoginContext(LoginContext loginContext) {
      _loginContext = loginContext;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }
  }

  public static class SuperAdmin extends KnownPrincipal {

    public SuperAdmin(String name) {
      super(name, null, null);
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public boolean isInRole(String role) {
      return true;
    }
  }

  public static class AnonymousPrincipal extends NutchGuiPrincipal {

    public AnonymousPrincipal() {
      super("Anonymous", null, new HashSet<String>());
    }

    @Override
    public boolean isAuthenticated() {
      return false;
    }

  }
}
