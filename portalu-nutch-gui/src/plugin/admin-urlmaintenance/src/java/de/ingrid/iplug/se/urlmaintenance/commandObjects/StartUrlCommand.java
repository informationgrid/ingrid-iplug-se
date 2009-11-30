package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public class StartUrlCommand extends StartUrl implements ICommandSerializer<StartUrl> {

  private Long _id;

  private List<LimitUrlCommand> _limitUrlCommands = new ArrayList<LimitUrlCommand>();

  private List<ExcludeUrlCommand> _excludeUrlCommands = new ArrayList<ExcludeUrlCommand>();

  private final IStartUrlDao _startUrlDao;

  private final ILimitUrlDao _limitUrlDao;

  private static final Log LOG = LogFactory.getLog(StartUrlCommand.class);

  private final IExcludeUrlDao _excludeUrlDao;

  public StartUrlCommand(IStartUrlDao startUrlDao, ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeUrlDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
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
  public void read(StartUrl startUrl) {
    setId(startUrl.getId());
    setProvider(startUrl.getProvider());
    setUrl(startUrl.getUrl());
    setCreated(startUrl.getCreated());
    setUpdated(startUrl.getUpdated());
    List<LimitUrl> limitUrls = startUrl.getLimitUrls();
    _limitUrlCommands.clear();
    for (LimitUrl limitUrl : limitUrls) {
      LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
      limitUrlCommand.read(limitUrl);
      addLimitUrlCommand(limitUrlCommand);
    }
    List<ExcludeUrl> excludeUrls = startUrl.getExcludeUrls();
    for (ExcludeUrl excludeUrl : excludeUrls) {
      ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
      excludeUrlCommand.read(excludeUrl);
      addExcludeUrlCommand(excludeUrlCommand);
    }
  }

  @Override
  public StartUrl write() {
    StartUrl startUrl = new StartUrl();
    startUrl.setProvider(getProvider());
    if (getId() != null) {
      LOG.info("load start url with id: " + getId());
      startUrl = _startUrlDao.getById(getId());
    }
    startUrl.setUrl(getUrl());
    startUrl.setCreated(getCreated());
    startUrl.setUpdated(getUpdated());

    handleLimitUrls(startUrl);
    handleExcludeUrls(startUrl);

    if (startUrl.getId() == null) {
      LOG.info("save new start url: " + startUrl);
      _startUrlDao.makePersistent(startUrl);
      _startUrlDao.flush();
    }
    return startUrl;

  }

  private void handleLimitUrls(StartUrl out) {
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
      if (limitUrl.getId() == null || !containsEntity(out.getLimitUrls(), limitUrl.getId())) {
        LOG.info("add new limit url: " + limitUrl);
        out.addLimitUrl(limitUrl);
      }
    }
  }

  private boolean containsEntity(List<? extends IdBase> entityCommands, Long idToTest) {
    for (IdBase idBase : entityCommands) {
      if (idToTest.equals(idBase.getId())) {
        return true;
      }
    }
    return false;
  }

  private void handleExcludeUrls(StartUrl out) {
    Iterator<ExcludeUrl> iterator = out.getExcludeUrls().iterator();
    while (iterator.hasNext()) {
      ExcludeUrl excludeUrlFromDb = (ExcludeUrl) iterator.next();
      boolean delete = true;
      for (ExcludeUrlCommand excludeUrlCommand : getExcludeUrlCommands()) {
        if (excludeUrlFromDb.getId().equals(excludeUrlCommand.getId())) {
          delete = false;
          break;
        }
      }
      if (delete) {
        iterator.remove();
        LOG.info("delete exclude url with id: " + excludeUrlFromDb.getId());
        _excludeUrlDao.makeTransient(excludeUrlFromDb);
      }
    }

    for (ExcludeUrlCommand excludeUrlCommand : getExcludeUrlCommands()) {
      ExcludeUrl excludeUrl = excludeUrlCommand.write();
      if (excludeUrl.getId() == null || !containsEntity(_excludeUrlCommands, excludeUrl.getId())) {
        LOG.info("add new exclude url: " + excludeUrl);
        out.addExcludeUrl(excludeUrl);
      }
    }
  }

}
