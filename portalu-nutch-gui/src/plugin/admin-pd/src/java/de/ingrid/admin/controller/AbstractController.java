package de.ingrid.admin.controller;

import org.apache.nutch.admin.NavigationSelector;

import de.ingrid.iplug.se.IKeys;

public class AbstractController extends NavigationSelector {

    public static String redirect(final String uri) {
        return IKeys.REDIRECT + uri;
    }
}
