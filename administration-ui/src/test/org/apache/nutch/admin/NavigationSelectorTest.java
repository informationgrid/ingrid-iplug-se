package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class NavigationSelectorTest extends TestCase {

  public void testSelectNavigation() throws Exception {
    String contextName = "/test5";
    Set<String> contextNames = new HashSet<String>();
    contextNames.add("/test1");
    contextNames.add("/test2");
    contextNames.add("/test3");
    contextNames.add("/test5-admin-system");
    contextNames.add("/test5-admin-instance");

    NavigationSelector navigationSelector = new NavigationSelector();
    Set<String> selectNavigations = navigationSelector.selectNavigations(
        contextName, contextNames);
    assertEquals(2, selectNavigations.size());
    assertTrue(selectNavigations.contains("/test5-admin-system"));
    assertTrue(selectNavigations.contains("/test5-admin-instance"));

  }
}
