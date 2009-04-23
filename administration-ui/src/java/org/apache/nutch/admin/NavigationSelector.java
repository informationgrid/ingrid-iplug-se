package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class NavigationSelector {

  @ModelAttribute("navigations")
  public String[] referenceDataNavigations(HttpSession session) {
    Set<String> set = selectNavigations(session);
    return set.toArray(new String[set.size()]);
  }

  @SuppressWarnings("unchecked")
  private Set<String> selectNavigations(HttpSession session) {
    Set<String> navigation = new HashSet<String>();
    Set<String> set = (Set<String>) session.getServletContext().getAttribute(
        "contextNames");
    String contextName = session.getServletContext().getServletContextName();
    int indexOf = contextName.indexOf("-");
    contextName = indexOf > -1 ? contextName.substring(0, indexOf - 1)
        : contextName;
    for (String string : set) {
      if (string.startsWith(contextName)) {
        navigation.add(string);
      }
    }
    return navigation;
  }
}
