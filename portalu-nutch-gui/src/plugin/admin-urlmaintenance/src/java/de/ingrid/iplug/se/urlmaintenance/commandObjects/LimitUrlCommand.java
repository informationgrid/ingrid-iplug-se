package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;

public class LimitUrlCommand extends LimitUrl implements
    ICommandSerializer<LimitUrl> {

  private Long _id;

  private final ILimitUrlDao _limitUrlDao;

  private static final Log LOG = LogFactory.getLog(LimitUrlCommand.class);

  public LimitUrlCommand(ILimitUrlDao limitUrlDao) {
    _limitUrlDao = limitUrlDao;
  }

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  @Override
  public void read(LimitUrl in) {
    setId(in.getId());
    setProvider(in.getProvider());
    setUrl(in.getUrl());
    setCreated(in.getCreated());
    setUpdated(in.getUpdated());
    setMetadatas(in.getMetadatas());
  }

  @Override
  public LimitUrl write() {
    LimitUrl out = new LimitUrl();
    out.setProvider(getProvider());
    if (getId() != null) {
      LOG.info("load limit url with id: " + getId());
      out = _limitUrlDao.getById(getId());
    }
    out.setUrl(getUrl());
    out.setCreated(getCreated());
    out.setUpdated(getUpdated());
    out.setMetadatas(getMetadatas());

    if (out.getId() == null) {
      LOG.info("save new limit url: " + out);
      _limitUrlDao.makePersistent(out);
    }

    return out;
  }

}
