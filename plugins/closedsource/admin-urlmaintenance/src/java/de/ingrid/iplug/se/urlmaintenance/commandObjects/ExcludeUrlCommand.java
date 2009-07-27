package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;

public class ExcludeUrlCommand extends ExcludeUrl implements
    ICommandSerializer<ExcludeUrl> {

  private Long _id;

  private final IExcludeUrlDao _excludeUrlDao;

  private static final Log LOG = LogFactory.getLog(ExcludeUrlCommand.class);

  public ExcludeUrlCommand(IExcludeUrlDao excludeUrlDao) {
    _excludeUrlDao = excludeUrlDao;
  }

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  @Override
  public void read(ExcludeUrl in) {
    setId(in.getId());
    setProvider(in.getProvider());
    setUrl(in.getUrl());
    setCreated(in.getCreated());
    setUpdated(in.getUpdated());
  }

  @Override
  public ExcludeUrl write() {
    ExcludeUrl out = new ExcludeUrl();
    out.setProvider(getProvider());
    if (getId() != null) {
      LOG.info("load exclude url with id: " + getId());
      out = _excludeUrlDao.getById(getId());
    }
    out.setUrl(getUrl());
    out.setCreated(getCreated());
    out.setUpdated(getUpdated());

    if (out.getId() == null) {
      LOG.info("save new exclude url: " + out);
      _excludeUrlDao.makePersistent(out);
    }

    return out;
  }

}
