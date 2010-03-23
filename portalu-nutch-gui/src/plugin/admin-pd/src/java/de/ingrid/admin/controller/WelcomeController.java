package de.ingrid.admin.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.se.IKeys;

@Controller
@SessionAttributes("plugDescription")
public class WelcomeController {

    private final PlugDescriptionService _service;

    @Autowired
    public WelcomeController(final PlugDescriptionService service) {
        _service = service;
    }

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final HttpSession session) throws Exception {
        if (session.getAttribute(IKeys.PLUG_DESCRIPTION) == null) {
            session.setAttribute(IKeys.PLUG_DESCRIPTION, _service.getCommandObect());
        }
        return IViews.WELCOME;
    }
}