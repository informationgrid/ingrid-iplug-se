package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;

@Controller
public class FinishController extends AbstractController {

    @RequestMapping(value = IUris.FINISH, method = RequestMethod.GET)
    public String getFinish() {
        return IViews.FINISH;
    }
}
