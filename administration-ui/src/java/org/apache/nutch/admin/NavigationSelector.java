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
    }
    System.out.println(set);
    return set.toArray(new String[set.size()]);
  }

  @SuppressWarnings("unchecked")
  @ModelAttribute("rootContexts")
  public String[] referenceDataInstances(HttpSession session) {
    Set<String> contextNames = (Set<String>) session.getServletContext()
        .getAttribute("contextNames");
    Set<String> set = selectRootContexts(contextNames);
    return set.toArray(new String[set.size()]);
  }

  public Set<String> selectRootContexts(Set<String> contextNames) {
    Set<String> set = new HashSet<String>();
    for (String name : contextNames) {
      int indexOf = name.lastIndexOf("/");
      if (indexOf == 0) {
        set.add(name);
      }
    }
    return set;
  }

  public Set<String> selectNavigations(String contextName,
      Set<String> allContextNames) {
    contextName = normalize(contextName);
    Set<String> navigation = new HashSet<String>();
    int indexOf = contextName.lastIndexOf("/");
    contextName = indexOf > 0 ? contextName.substring(0, indexOf) : contextName;
    for (String anotherContextName : allContextNames) {
      anotherContextName = normalize(anotherContextName);
      int anotherIndexOf = anotherContextName.lastIndexOf("/");
      String tmpContextName = anotherIndexOf > 0 ? anotherContextName
          .substring(0, anotherIndexOf) : anotherContextName;
      if (tmpContextName.equals(contextName)) {
        navigation.add(anotherContextName);
      }
    }
    return navigation;
  }

  private String normalize(String contextName) {
    int indexOf = contextName.lastIndexOf("/");
    if (indexOf == contextName.length() - 1) {
      System.out.println(contextName);
      contextName = contextName.substring(0, contextName.length() - 1);
    }
    return contextName;
  }

}
