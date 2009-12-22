package de.ingrid.admin.service;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

@Service
public class PlugDescriptionService {

	protected static final Logger LOG = Logger
			.getLogger(PlugDescriptionService.class);

	private final File _plugDescriptionFile;

	private PlugDescription _plugDescription;


	public PlugDescriptionService() throws IOException {
		_plugDescriptionFile = new File(System.getProperty(IKeys.PLUG_DESCRIPTION));
		if (existsPlugDescription()) {
			_plugDescription = loadPlugDescription();
		} else {
			LOG.warn("plug description does not exist. please create one via ui setup.");
		}
	}

	public PlugDescription getPlugDescription() throws IOException {
		if (_plugDescription == null && existsPlugDescription()) {
			_plugDescription = loadPlugDescription();
		}
		return _plugDescription;
	}

	@SuppressWarnings("unchecked")
	public void savePlugDescription(final PlugDescription plugDescription)
			throws Exception {
		LOG.info("saving plug description.");
		PlugDescription tmpDesc = plugDescription;
		if (plugDescription instanceof PlugdescriptionCommandObject) {
            tmpDesc = new PlugDescription();
            tmpDesc.putAll(plugDescription);
		}
		PlugdescriptionSerializer serializer = new PlugdescriptionSerializer();
		serializer.serialize(tmpDesc, _plugDescriptionFile);
		// load again to set the serialized folder
		_plugDescription = loadPlugDescription();
	}

	public boolean existsPlugDescription() {
		return _plugDescriptionFile.exists();
	}

	private PlugDescription loadPlugDescription() throws IOException {
		LOG.info("load plugdescription from file: "
				+ _plugDescriptionFile.getAbsolutePath());
		return new PlugdescriptionSerializer()
				.deSerialize(_plugDescriptionFile);
	}
}
