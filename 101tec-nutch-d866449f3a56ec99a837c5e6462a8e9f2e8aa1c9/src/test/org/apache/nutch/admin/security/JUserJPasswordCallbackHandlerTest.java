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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mortbay.http.HttpRequest;

public class JUserJPasswordCallbackHandlerTest extends TestCase {

  @Mock
  private HttpRequest _httpRequest;

  @Override
  protected void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  public void testCallback() throws Exception {
    Mockito.when(_httpRequest.getParameter("j_username")).thenReturn("foo");
    Mockito.when(_httpRequest.getParameter("j_password")).thenReturn("bar");

    CallbackHandler callbackHandler = new JUserJPasswordCallbackHandler(
            _httpRequest);
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("user name:");
    callbacks[1] = new PasswordCallback("password:", false);
    callbackHandler.handle(callbacks);
    assertEquals("foo", ((NameCallback) callbacks[0]).getName());
    assertEquals("bar", new String(((PasswordCallback) callbacks[1])
            .getPassword()));

  }
}
