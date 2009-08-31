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
package org.apache.nutch.admin;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ModelAttribute;

public class NavigationSelector {

  public static class Navigation implements Comparable<Navigation> {

    private String _link;
    private String _name;

    public Navigation(String link, String name) {
      _link = link;
      _name = name;
    }

    public String getLink() {
      return _link;
    }

    public void setLink(String link) {
      _link = link;
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_link == null) ? 0 : _link.hashCode());
      result = prime * result + ((_name == null) ? 0 : _name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Navigation other = (Navigation) obj;
      if (_link == null) {
        if (other._link != null)
          return false;
      } else if (!_link.equals(other._link))
        return false;
      if (_name == null) {
        if (other._name != null)
          return false;
      } else if (!_name.equals(other._name))
        return false;
      return true;
    }

    @Override
    public int compareTo(Navigation o) {
      return _name.compareTo(o._name);
    }

    @Override
    public String toString() {
      return _link + "#" + _name;
    }

  }

  @ModelAttribute("selectedInstance")
  public String referenceSelectedInstance(HttpSession session) {
    String contextName = session.getServletContext().getServletContextName();
    String[] split = contextName.split("/");
    return split[1];
  }

  @ModelAttribute("selectedComponent")
  public String referenceSelectedComponent(HttpSession session) {
    String contextName = session.getServletContext().getServletContextName();
    String[] split = contextName.split("/");
    contextName = split[1];
    if (split.length > 2) {
      contextName = split[2];
    }
    return contextName;
  }

  @SuppressWarnings("unchecked")
  @ModelAttribute("componentNavigation")
  public Set<Navigation> referenceComponents(HttpSession session) {
    String contextPrefix = session.getServletContext().getServletContextName()
        .split("/")[1];
    Set<String> contextNames = (Set<String>) session.getServletContext()
        .getAttribute("contextNames");
    return selectComponentNavigation(contextPrefix, contextNames);
  }

  public Set<Navigation> selectComponentNavigation(String contextPrefix,
      Set<String> contextNames) {
    Set<Navigation> ret = new TreeSet<Navigation>();
    for (String contextName : contextNames) {
      String[] split = contextName.split("/");
      String prefix = split[1];
      // e.g. /general/admin-system
      if (split.length > 2) {
        String component = split[2];
        if (prefix.equals(contextPrefix)) {
          Navigation navigation = new Navigation(contextName, component);
          ret.add(navigation);
        }
      }
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  @ModelAttribute("instanceNavigation")
  public Set<Navigation> referenceGeneralComponents(HttpSession session) {
    Set<String> contextNames = (Set<String>) session.getServletContext()
        .getAttribute("contextNames");
    return selectInstanceNavigation(contextNames);
  }

  public Set<Navigation> selectInstanceNavigation(Set<String> contextNames) {
    Set<Navigation> ret = new TreeSet<Navigation>();
    for (String contextName : contextNames) {
      String[] split = contextName.split("/");
      // e.g. /general
      if (split.length == 2) {
        String component = split[1];
        Navigation navigation = new Navigation(contextName, component);
        ret.add(navigation);
      }
    }
    return ret;
  }

}
