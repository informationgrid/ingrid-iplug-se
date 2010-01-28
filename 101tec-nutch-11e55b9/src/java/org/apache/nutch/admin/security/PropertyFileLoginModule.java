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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.apache.nutch.admin.security.NutchGuiPrincipal.KnownPrincipal;

public class PropertyFileLoginModule extends AbstractLoginModule {

  private Map<String, KnownPrincipal> _knownUsers = new HashMap<String, KnownPrincipal>();

  @Override
  public void initialize(Subject subject, CallbackHandler callbackHandler,
          Map<String, ?> sharedState, Map<String, ?> options) {
    super.initialize(subject, callbackHandler, sharedState, options);
    InputStream resourceAsStream = PropertyFileLoginModule.class
            .getResourceAsStream("/" + (String) options.get("file"));
    Properties properties = new Properties();
    try {
      properties.load(resourceAsStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Set<Object> keySet = properties.keySet();
    for (Object user : keySet) {
      String userProps = properties.get(user).toString();
      String[] splits = userProps.split(",");
      String password = splits[0];
      String[] roles = new String[splits.length - 1];
      System.arraycopy(splits, 1, roles, 0, roles.length);
      Set<String> set = new HashSet<String>(Arrays.asList(roles));
      KnownPrincipal knownPrincipal = new NutchGuiPrincipal.KnownPrincipal(user
              .toString(), password, set);
      _knownUsers.put(user.toString(), knownPrincipal);
    }
  }

  @Override
  protected NutchGuiPrincipal authenticate(String userName, String password) {
    NutchGuiPrincipal principal = new NutchGuiPrincipal.AnonymousPrincipal();
    KnownPrincipal knownPrincipal = _knownUsers.get(userName);
    if (knownPrincipal != null) {
      String knownPassword = knownPrincipal.getPassword();
      if (knownPassword.equals(new String(password))) {
        principal = knownPrincipal;
      }
    }
    return principal;
  }

}
