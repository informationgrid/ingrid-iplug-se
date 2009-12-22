package de.ingrid.admin.controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.CommunicationValidator;
import de.ingrid.admin.validation.IErrorKeys;

@Controller
public class CommunicationConfigurationController extends AbstractController {

    public static final int DEFAULT_TIMEOUT = 10;

    public static final int DEFAULT_MAXIMUM_SIZE = 1048576;

    public static final int DEFAULT_THREAD_COUNT = 100;

    private final CommunicationService _communicationService;

    private final CommunicationValidator _validator;

    @Autowired
    public CommunicationConfigurationController(final CommunicationService communicationService,
            final CommunicationValidator validator) {
        _communicationService = communicationService;
        _validator = validator;
    }

    @ModelAttribute("communication")
    public CommunicationCommandObject createCommandObject() throws Exception {
        // create command object
        final CommunicationCommandObject commandObject = new CommunicationCommandObject();

        // create the xpath
        final XPathService communication = new XPathService();

        // open communication file
        final File communicationFile = _communicationService.getCommunicationFile();

        // bus count
        Integer count = 0;

        if (communicationFile.exists()) {
            // only save proxy service url
            communication.registerDocument(communicationFile);
            commandObject.setProxyServiceUrl(communication.parseAttribute("/communication/client", "name"));
            count = getBusCount(communication);
        }

        if (count < 1) {
            // open template file
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
            // save all default values
            if (null == commandObject.getProxyServiceUrl()) {
                commandObject.setProxyServiceUrl(communication.parseAttribute("/communication/client", "name"));
            }
            commandObject.setBusProxyServiceUrl(communication.parseAttribute(
                    "/communication/client/connections/server", "name"));
            commandObject.setIp(communication.parseAttribute("/communication/client/connections/server/socket", "ip"));
            commandObject.setPort(Integer.parseInt(communication.parseAttribute(
                    "/communication/client/connections/server/socket", "port")));
        }
        String proxyServiceUrl = commandObject.getProxyServiceUrl();
        final String userName = System.getProperty("user.name");
        proxyServiceUrl = proxyServiceUrl.endsWith("_" + userName) ? proxyServiceUrl : proxyServiceUrl + "_" + userName;
        commandObject.setProxyServiceUrl(proxyServiceUrl);

        // return command object
        return commandObject;
    }

    @ModelAttribute("busses")
    public List<CommunicationCommandObject> existingBusses() throws Exception {
        // open communication file
        final File communicationFile = _communicationService.getCommunicationFile();
        if (!communicationFile.exists()) {
            return null;
        }
        // create xpath service for xml
        final XPathService communication = new XPathService();
        communication.registerDocument(communicationFile);
        // determine count of ibusses
        final int count = communication.countNodes("/communication/client/connections/server");
        // create List of communication
        final List<CommunicationCommandObject> busses = new ArrayList<CommunicationCommandObject>();
        // and get all information about each ibus
        for (int i = 0; i < count; i++) {
            final CommunicationCommandObject bus = new CommunicationCommandObject();
            bus.setBusProxyServiceUrl(communication.parseAttribute("/communication/client/connections/server", "name",
                    i));
            bus.setIp(communication.parseAttribute("/communication/client/connections/server/socket", "ip", i));
            bus.setPort(Integer.parseInt(communication.parseAttribute(
                    "/communication/client/connections/server/socket", "port", i)));
            busses.add(bus);
        }
        // return all busses
        return busses;
    }

    @RequestMapping(value = IUris.COMMUNICATION, method = RequestMethod.GET)
    public String getCommunication() {
        return IViews.COMMUNICATION;
    }

    @RequestMapping(value = IUris.COMMUNICATION, method = RequestMethod.POST)
    public String postCommunication(final ModelMap modelMap,
            @ModelAttribute("communication") final CommunicationCommandObject commandObject, final Errors errors,
            @RequestParam(value = "action", required = false) final String action,
            @RequestParam(value = "id", required = false) final Integer id)
            throws Exception {
        if (null != action && !"".equals(action)) {
            final File communicationFile = _communicationService.getCommunicationFile();
            final XPathService communication = openCommunication(communicationFile);

            boolean tryToAdd = false;

            if ("submit".equals(action)) {
                // set proxy url
                if (_validator.validateProxyUrl(errors).hasErrors()) {
                    return IViews.COMMUNICATION;
                }
                setProxyUrl(communication, commandObject.getProxyServiceUrl());

                if (!communicationFile.exists() || getBusCount(communication) == 0) {
                    tryToAdd = true;
                }
            }

            if ("add".equals(action) || tryToAdd) {
                // set proxy url
                if (!_validator.validateProxyUrl(errors).hasErrors()) {
                    setProxyUrl(communication, commandObject.getProxyServiceUrl());
                }
                // add new bus
                if (_validator.validateBus(errors).hasErrors()) {
                    return IViews.COMMUNICATION;
                }
                if (communicationFile.exists()) {
                    addBus(communication, commandObject.getBusProxyServiceUrl(), commandObject.getIp(), commandObject
                            .getPort(), null);
                } else {
                    setBus(communication, commandObject.getBusProxyServiceUrl(), commandObject.getIp(), commandObject
                            .getPort(), 0);
                }
            } else if ("delete".equals(action)) {
                // delete bus
                deleteBus(communication, id);
            } else if ("set".equals(action)) {
                // set base bus
                switchBusses(communication, 0, id);
            }

            // save the new data
            communication.store(communicationFile);

            // update command object and existing busses
            modelMap.addAttribute("communication", createCommandObject());
            modelMap.addAttribute("busses", existingBusses());

            // submit complete?!
            if ("submit".equals(action)) {
                if (!communicationFile.exists() || getBusCount(communication) == 0) {
                    _validator.rejectError(errors, "busProxyServiceUrl", IErrorKeys.MISSING);
                    return IViews.COMMUNICATION;
                }
                // restart communication interface
                _communicationService.restart();
                if (_communicationService.isConnected()) {
                    // redirect to next step
                    return redirect(IUris.WORKING_DIR);
                }
            }
        }

        return IViews.COMMUNICATION;
    }

    private final XPathService openCommunication(final File communicationFile) throws Exception {
        // first of all create directories if necessary
        if (communicationFile.getParentFile() != null) {
            if (!communicationFile.getParentFile().exists()) {
                communicationFile.getParentFile().mkdirs();
            }
        }

        // open template xml or communication file
        final XPathService communication = new XPathService();
        if (!communicationFile.exists()) {
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
        } else {
            communication.registerDocument(communicationFile);
        }

        return communication;
    }

    private void setProxyUrl(final XPathService communication, final String proxyUrl) throws Exception {
        communication.setAttribute("/communication/client", "name", proxyUrl);
    }

    private void setBus(final XPathService communication, final String proxyUrl, final String ip, final Integer port,
            final Integer id) throws Exception {
        communication.setAttribute("/communication/client/connections/server", "name", proxyUrl, id);
        communication.setAttribute("/communication/client/connections/server/socket", "port", "" + port, id);
        communication.setAttribute("/communication/client/connections/server/socket", "ip", ip, id);
    }

    private void switchBusses(final XPathService communication, final Integer idA, final Integer idB) throws Exception {
        // get old values
        final String proxyUrlA = communication.parseAttribute("/communication/client/connections/server", "name", idA);
        final Integer portA = Integer.parseInt(communication.parseAttribute(
                "/communication/client/connections/server/socket", "port", idA));
        final String ipA = communication.parseAttribute("/communication/client/connections/server/socket", "ip", idA);
        // get new values
        final String proxyUrlB = communication.parseAttribute("/communication/client/connections/server", "name", idB);
        final Integer portB = Integer.parseInt(communication.parseAttribute(
                "/communication/client/connections/server/socket", "port", idB));
        final String ipB = communication.parseAttribute("/communication/client/connections/server/socket", "ip", idB);

        // switch positions
        setBus(communication, proxyUrlA, ipA, portA, idB);
        setBus(communication, proxyUrlB, ipB, portB, idA);
    }

    private void addBus(final XPathService communication, final String proxyUrl, final String ip, final Integer port,
            Integer id) throws Exception {
        if (null == id) {
            // determine count of ibusses (=index of new ibus)
            id = getBusCount(communication);
        }

        // create default nodes and attributes
        communication.addNode("/communication/client/connections", "server");
        communication.addNode("/communication/client/connections/server", "socket", id);
        communication.addAttribute("/communication/client/connections/server/socket", "timeout", "" + DEFAULT_TIMEOUT,
                id);
        communication.addNode("/communication/client/connections/server", "messages", id);
        communication.addAttribute("/communication/client/connections/server/messages", "maximumSize", ""
                + DEFAULT_MAXIMUM_SIZE, id);
        communication.addAttribute("/communication/client/connections/server/messages", "threadCount", ""
                + DEFAULT_THREAD_COUNT, id);

        // add a new bus
        communication.addAttribute("/communication/client/connections/server", "name", proxyUrl, id);
        communication.addAttribute("/communication/client/connections/server/socket", "port", "" + port, id);
        communication.addAttribute("/communication/client/connections/server/socket", "ip", ip, id);
    }

    private void deleteBus(final XPathService communication, final Integer id) throws Exception {
        communication.removeNode("/communication/client/connections/server", id);
    }

    private Integer getBusCount(final XPathService communication) throws Exception {
        return communication.countNodes("/communication/client/connections/server");
    }
}
