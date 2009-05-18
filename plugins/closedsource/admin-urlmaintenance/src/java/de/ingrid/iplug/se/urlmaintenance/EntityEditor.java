package de.ingrid.iplug.se.urlmaintenance;

import java.beans.PropertyEditorSupport;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;

public class EntityEditor extends PropertyEditorSupport {

  private final IDao<? extends IdBase> _dao;

  public EntityEditor(IDao<? extends IdBase> dao) {
    _dao = dao;
  }

  public String getAsText() {
    Object object = getValue();
    IdBase base = (IdBase) object;
    return base != null && base.getId() != null ? base.getId().toString() : "";
  }

  public void setAsText(String text) {
    Long id = -1L;
    try {
      id = Long.parseLong(text);
    } catch (Exception e) {
      // TODO: handle exception
    }
    if (id != -1) {
      IdBase base = _dao.getById(id);
      setValue(base);
    }
  }

}