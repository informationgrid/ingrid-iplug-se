package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public class StartUrlCommand extends StartUrl implements
    ICommandSerializer<StartUrl> {

  private Long _id;

  private List<LimitUrlCommand> _limitUrlCommands = new ArrayList<LimitUrlCommand>();

  private List<ExcludeUrlCommand> _excludeUrlCommands = new ArrayList<ExcludeUrlCommand>();

  private final IStartUrlDao _startUrlDao;

  private final ILimitUrlDao _limitUrlDao;

  private static final Log LOG = LogFactory.getLog(StartUrlCommand.class);

  public StartUrlCommand(IStartUrlDao startUrlDao, ILimitUrlDao limitUrlDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
  }

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public List<LimitUrlCommand> getLimitUrlCommands() {
    return _limitUrlCommands;
  }

  public void setLimitUrlCommands(List<LimitUrlCommand> limitUrlCommands) {
    _limitUrlCommands = limitUrlCommands;
  }

  public List<ExcludeUrlCommand> getExcludeUrlCommands() {
    return _excludeUrlCommands;
  }

  public void setExcludeUrlCommands(List<ExcludeUrlCommand> excludeUrlCommands) {
    _excludeUrlCommands = excludeUrlCommands;
  }

  public void addLimitUrlCommand(LimitUrlCommand limitUrlCommand) {
    _limitUrlCommands.add(limitUrlCommand);
  }

  public void addExcludeUrlCommand(ExcludeUrlCommand excludeUrlCommand) {
    _excludeUrlCommands.add(excludeUrlCommand);
  }

  @Override
  public void read(StartUrl in) {
    setId(in.getId());
    setProvider(in.getProvider());
    setUrl(in.getUrl());
    setCreated(in.getCreated());
    setUpdated(in.getUpdated());
    List<LimitUrl> limitUrls = in.getLimitUrls();
    for (LimitUrl limitUrl : limitUrls) {
      LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
      limitUrlCommand.read(limitUrl);
      addLimitUrlCommand(limitUrlCommand);
    }
  }

  @Override
  public StartUrl write() {
    StartUrl out = new StartUrl();
    out.setProvider(getProvider());
    if (getId() != null) {
      LOG.info("load start url with id: " + getId());
      out = _startUrlDao.getById(getId());
    }
    out.setUrl(getUrl());
    out.setCreated(getCreated());
    out.setUpdated(getUpdated());

    Iterator<LimitUrl> iterator = out.getLimitUrls().iterator();
    while (iterator.hasNext()) {
      LimitUrl limitUrlFromDb = (LimitUrl) iterator.next();
      boolean delete = true;
      for (LimitUrlCommand limitUrlCommand : getLimitUrlCommands()) {
        if (limitUrlFromDb.getId().equals(limitUrlCommand.getId())) {
          delete = false;
          break;
        }
      }
      if (delete) {
        iterator.remove();
        LOG.info("delete limit url with id: " + limitUrlFromDb.getId());
        _limitUrlDao.makeTransient(limitUrlFromDb);
      }
    }

    for (LimitUrlCommand limitUrlCommand : getLimitUrlCommands()) {
      LimitUrl limitUrl = limitUrlCommand.write();
      if (limitUrl.getId() == null) {
        LOG.info("add new limit url: " + limitUrl);
        out.addLimitUrl(limitUrl);
      }
    }

    if (out.getId() == null) {
      LOG.info("save new start url: " + out);
      _startUrlDao.makePersistent(out);
    }
    return out;

  }

}
