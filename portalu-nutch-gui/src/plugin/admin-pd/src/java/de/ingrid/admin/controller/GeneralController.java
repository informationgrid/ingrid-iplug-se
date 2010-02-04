package de.ingrid.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
public class GeneralController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final PlugDescValidator _validator;

    @Autowired
    public GeneralController(final CommunicationService communicationInterface, final PlugDescValidator validator)
            throws Exception {
        _communicationInterface = communicationInterface;
        _validator = validator;
    }

    @ModelAttribute("partners")
    public List<Partner> getPartners() throws Exception {
        return Utils.getPartners(_communicationInterface.getIBus());
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.GET)
    public String getGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {

        // set up proxy service url
        commandObject.setProxyServiceURL(_communicationInterface.getPeerName());

        return IViews.GENERAL;
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.POST)
    public String postGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {
        // add partners and providers
        for (final Partner partner : partners) {
            commandObject.addPartner(partner.getShortName());
        }
        commandObject.addProvider("all");

        if (_validator.validateGeneral(errors).hasErrors()) {
            return getGeneral(modelMap, commandObject, errors, partners);
        }

        // add data type includes
        return redirect(IUris.SAVE);
    }
}
