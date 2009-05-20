package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class NavigationSelectorTest extends TestCase {

  public void testSelectNavigation() throws Exception {
    String contextName = "/test5";
    Set<String> contextNames = new HashSet<String>();
    contextNames.add("/test0/abc");
    contextNames.add("/test0/");
    contextNames.add("/test1");
    contextNames.add("/test2");
    contextNames.add("/test3");
    contextNames.add("/test5/admin-system");
    contextNames.add("/test5/admin-instance");
    contextNames.add("/test5/");
    

    NavigationSelector navigationSelector = new NavigationSelector();
    Set<String> selectNavigations = navigationSelector.selectNavigations(
        contextName, contextNames);
    System.out.println(selectNavigations);
    assertEquals(2, selectNavigations.size());
    assertTrue(selectNavigations.contains("/test5/admin-system"));
    assertTrue(selectNavigations.contains("/test5/admin-instance"));

  }
  
  public void testSelectRootContexts() throws Exception {
    String contextName = "/test5";
    Set<String> contextNames = new HashSet<String>();
    contextNames.add("/test1");
    contextNames.add("/test1/abc");
    contextNames.add("/test2");
    contextNames.add("/test2/foo");
    contextNames.add("/test2/bar");

    NavigationSelector navigationSelector = new NavigationSelector();
    Set<String> selectRootContexts = navigationSelector
        .selectRootContexts(contextNames);
    assertEquals(2, selectRootContexts.size());
    assertTrue(selectRootContexts.contains("/test1"));
    assertTrue(selectRootContexts.contains("/test2"));
  }
}
