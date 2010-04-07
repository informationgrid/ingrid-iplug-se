package de.ingrid.admin.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;

/**
 * This class is used to redirect pages that aren't supposed to be shown
 * here, but which is the default behaviour of the base-webapp.
 * 
 * @author Andre Wallat
 *
 */

@Controller
public class DummyController {

    @RequestMapping(value = IUris.PARTNER, method = RequestMethod.GET)
    public String redirectPartner(final HttpSession session) throws Exception {
        return IViews.SAVE;
    }
    
    @RequestMapping(value = IUris.SCHEDULING, method = RequestMethod.GET)
    public String redirectScheduling(final HttpSession session) throws Exception {
        return IViews.FINISH;
    }
}
