package de.ingrid.admin.controller;

import java.io.File;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.IKeys;

@Controller
@SessionAttributes("plugDescription")
public class WelcomeController extends AbstractController {

    private static PlugdescriptionCommandObject _plugDescription;

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final HttpSession session) throws Exception {
        loadPlugDescription(session);
        return IViews.WELCOME;
    }

    public PlugdescriptionCommandObject loadPlugDescription(final HttpSession session) throws Exception {
        if (_plugDescription == null) {
            _plugDescription = new PlugdescriptionCommandObject(new File(System.getProperty("plugDescription")));
        }
        if (session.getAttribute(IKeys.PLUG_DESCRIPTION) == null) {
            session.setAttribute(IKeys.PLUG_DESCRIPTION, _plugDescription);
        }
        return _plugDescription;
    }
}