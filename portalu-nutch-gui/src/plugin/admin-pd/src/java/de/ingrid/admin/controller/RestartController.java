package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;

@Controller
public class RestartController extends AbstractController {

    @RequestMapping(value = IUris.RESTART, method = RequestMethod.GET)
    public String getWorkingDir() {
        return IViews.RESTART;
    }
}
