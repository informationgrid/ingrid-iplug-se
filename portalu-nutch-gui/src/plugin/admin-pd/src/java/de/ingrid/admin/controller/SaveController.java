package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IRecordLoader;

@Controller
@SessionAttributes("plugDescription")
public class SaveController extends AbstractController {

    private final IConfigurable[] _configurables;

    private final HeartBeatPlug _plug;

    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public SaveController(final HeartBeatPlug plug, final PlugDescriptionService plugDescriptionService,
            final IConfigurable... configurables) {
        _plug = plug;
        _plugDescriptionService = plugDescriptionService;
        _configurables = configurables;
    }

    @RequestMapping(value = IUris.SAVE, method = RequestMethod.GET)
    public String save() {
        return IViews.SAVE;
    }

    @RequestMapping(value = IUris.SAVE, method = RequestMethod.POST)
    public String postSave(
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription)
            throws Exception {

        boolean restart = false;

        // set class and record loader
        plugDescription.setIPlugClass(_plug.getClass().getName());
        plugDescription.setRecordLoader(_plug instanceof IRecordLoader);

        // if port has changed show a message to the user to restart the iPlug
        if (plugDescription.getIplugAdminGuiPort() != plugDescription.getOriginalPort() || !plugDescription.getWorkinDirectory().equals(plugDescription.getOriginalWorkingDir())) {
            restart = true;
        }

        // save plug description
        _plugDescriptionService.savePlugDescription(plugDescription);

        // redirect to the restart page
        if (restart) {
          return redirect(IUris.RESTART);
        }

        // reconfigure all configurables
        for (final IConfigurable configurable : _configurables) {
            configurable.configure(_plugDescriptionService.getPlugDescription());
        }

        // start heart beat
        _plug.startHeartBeats();

        return redirect(IUris.FINISH);
    }
}
