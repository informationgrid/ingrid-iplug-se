package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;

public interface IPartnerDao extends IDao<Partner> {

  Partner getByName(String string);

}
