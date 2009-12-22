package de.ingrid.admin.controller;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;

@Controller
public class AdminToolsController extends AbstractController {

    protected static final Logger LOG = Logger.getLogger(AdminToolsController.class);

    private final CommunicationService _communication;

    private final HeartBeatPlug _plug;

    @Autowired
    public AdminToolsController(final CommunicationService communication, final HeartBeatPlug plug,
            final PlugDescriptionService plugDescriptionService) throws Exception {
        _communication = communication;
        _plug = plug;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.GET)
    public String getCommSetup(final ModelMap modelMap) {
        modelMap.addAttribute("connected", _communication.isConnected());
        return IViews.COMM_SETUP;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.POST)
    public String postCommSetup(@RequestParam("action") final String action) throws Exception {
        if ("shutdown".equals(action)) {
            _communication.shutdown();
        } else if ("restart".equals(action)) {
            _communication.restart();
        } else if ("start".equals(action)) {
            _communication.start();
        }
        return redirect(IUris.COMM_SETUP);
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.GET)
    public String getHeartbeat(final ModelMap modelMap) {
        modelMap.addAttribute("enabled", _plug.sendingHeartBeats());
        modelMap.addAttribute("accurate", _plug.sendingAccurate());
        return IViews.HEARTBEAT_SETUP;
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.POST)
    public String setHeartBeat(@RequestParam("action") final String action) throws IOException {
        if ("start".equals(action)) {
            _plug.startHeartBeats();
        } else if ("stop".equals(action)) {
            _plug.stopHeartBeats();
        } else if ("restart".equals(action)) {
            _plug.stopHeartBeats();
            _plug.startHeartBeats();
        }
        return redirect(IUris.HEARTBEAT_SETUP);
    }
}
