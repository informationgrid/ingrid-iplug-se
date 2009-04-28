package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ModelAttribute;

public class NavigationSelector {

  @SuppressWarnings("unchecked")
  @ModelAttribute("navigations")
  public String[] referenceDataNavigations(HttpSession session) {
    String contextName = session.getServletContext().getServletContextName();
    Set<String> contextNames = (Set<String>) session.getServletContext()
        .getAttribute("contextNames");
    Set<String> set = new HashSet<String>();
    if (contextNames != null) {
      set = selectNavigations(contextName, contextNames);
      set.add(contextName);
    }
    return set.toArray(new String[set.size()]);
  }

  public Set<String> selectNavigations(String contextName,
      Set<String> allContextNames) {
    Set<String> navigation = new HashSet<String>();

    int indexOf = contextName.indexOf("-");
    contextName = indexOf > -1 ? contextName.substring(0, indexOf)
        : contextName;
    for (String anotherContextName : allContextNames) {
      int anotherIndexOf = anotherContextName.indexOf("-");
      String tmpContextName = anotherIndexOf > -1 ? anotherContextName
          .substring(0, anotherIndexOf) : anotherContextName;
      if (tmpContextName.equals(contextName)) {
        navigation.add(anotherContextName);
      }
    }
    return navigation;
  }

}
