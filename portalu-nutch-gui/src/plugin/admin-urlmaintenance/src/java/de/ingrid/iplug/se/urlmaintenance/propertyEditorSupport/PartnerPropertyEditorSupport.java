package de.ingrid.iplug.se.urlmaintenance.propertyEditorSupport;

import java.beans.PropertyEditorSupport;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;

public class PartnerPropertyEditorSupport extends PropertyEditorSupport {

  private final IPartnerDao _partnerDao;

  public PartnerPropertyEditorSupport(IPartnerDao partnerDao) {
    super();
    _partnerDao = partnerDao;
  }

  @Override
  public void setAsText(String incomming) {
    if (incomming == null) {
      return;
    }
    int id = Integer.parseInt(incomming);

    setValue(_partnerDao.getById((long) id));
  }

  @Override
  public String getAsText() {
    Partner partner = (Partner) getValue();
    if (partner == null) {
      return null;
    }
    return partner.getId().toString();
  }
}
