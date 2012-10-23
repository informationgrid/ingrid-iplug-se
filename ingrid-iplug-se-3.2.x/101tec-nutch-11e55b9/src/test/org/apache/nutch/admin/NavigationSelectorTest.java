package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.nutch.admin.NavigationSelector.Navigation;

public class NavigationSelectorTest extends TestCase {

  public void testSelectNavigation() throws Exception {
    String contextName = "test5";
    Set<String> contextNames = new HashSet<String>();
    contextNames.add("/test0/abc");
    contextNames.add("/test0/");
    contextNames.add("/test0");
    contextNames.add("/test1");
    contextNames.add("/test2");
    contextNames.add("/test3");
    contextNames.add("/test5/admin-system");
    contextNames.add("/test5/admin-instance");
    contextNames.add("/test5/");

    NavigationSelector navigationSelector = new NavigationSelector();
    Set<Navigation> selectNavigations = navigationSelector
            .selectComponentNavigation(contextName, contextNames);
    assertEquals(2, selectNavigations.size());
    assertTrue(selectNavigations.contains(new Navigation("/test5/admin-system",
            "admin-system")));
    assertTrue(selectNavigations.contains(new Navigation(
            "/test5/admin-instance", "admin-instance")));

    selectNavigations = navigationSelector.selectComponentNavigation("test5",
            contextNames);
    assertEquals(2, selectNavigations.size());

    selectNavigations = navigationSelector
            .selectInstanceNavigation(contextNames);
    assertEquals(5, selectNavigations.size());
    assertTrue(selectNavigations.contains(new Navigation("/test0", "test0")));
    assertTrue(selectNavigations.contains(new Navigation("/test1", "test1")));
    assertTrue(selectNavigations.contains(new Navigation("/test2", "test2")));
    assertTrue(selectNavigations.contains(new Navigation("/test3", "test3")));
    assertTrue(selectNavigations.contains(new Navigation("/test5", "test5")));
  }

}
