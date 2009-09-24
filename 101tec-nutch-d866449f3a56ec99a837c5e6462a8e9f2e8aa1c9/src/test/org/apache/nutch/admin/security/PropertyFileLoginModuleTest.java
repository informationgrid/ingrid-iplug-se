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

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mortbay.http.HttpRequest;

public class PropertyFileLoginModuleTest extends TestCase {

  private LoginContext _loginContext;

  @Mock
  private HttpRequest _httpRequest;

  @Override
  protected void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    System.setProperty("java.security.auth.login.config", System
            .getProperty("user.dir")
            + "/conf/nutchgui.auth");
    JUserJPasswordCallbackHandler handler = new JUserJPasswordCallbackHandler(
            _httpRequest);
    _loginContext = new LoginContext("PropertyFileLogin", handler);
  }

  public void testLoginFailed() throws LoginException {
    Mockito.when(_httpRequest.getParameter("j_username")).thenReturn("foo");
    Mockito.when(_httpRequest.getParameter("j_password")).thenReturn("foobar");
    try {
      _loginContext.login();
    } catch (LoginException e) {
    }
  }

  public void testLogin() throws LoginException {
    Mockito.when(_httpRequest.getParameter("j_username")).thenReturn("foo");
    Mockito.when(_httpRequest.getParameter("j_password")).thenReturn("bar");
    _loginContext.login();
  }

  public void testLogout() throws LoginException {
    Mockito.when(_httpRequest.getParameter("j_username")).thenReturn("foo");
    Mockito.when(_httpRequest.getParameter("j_password")).thenReturn("bar");
    _loginContext.login();
    _loginContext.logout();
  }

}
