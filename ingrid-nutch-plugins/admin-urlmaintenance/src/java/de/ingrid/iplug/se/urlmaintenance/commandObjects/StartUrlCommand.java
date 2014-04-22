package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

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
      if (limitUrl.getDeleted() != null) {
        continue;
      }
      LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
      limitUrlCommand.read(limitUrl);
      addLimitUrlCommand(limitUrlCommand);
    }
    List<ExcludeUrl> excludeUrls = startUrl.getExcludeUrls();
    for (ExcludeUrl excludeUrl : excludeUrls) {
      if (excludeUrl.getDeleted() != null) {
        continue;
      }
      ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
      excludeUrlCommand.read(excludeUrl);
      addExcludeUrlCommand(excludeUrlCommand);
    }
  }

  @Override
  public StartUrl write() {
    StartUrl newStartUrl = null;
    StartUrl oldStartUrl = null;
    if (getId() != null) {
      LOG.info("load start url with id: " + getId());
      oldStartUrl = _startUrlDao.getById(getId());
      if (this.getUrl().equals(oldStartUrl.getUrl())) {
          newStartUrl = oldStartUrl; 
      } else {
          // mark existing old limit and exclude URls deleted
          Iterator<LimitUrl> iterator = oldStartUrl.getLimitUrls().iterator();
          while (iterator.hasNext()) {
            LimitUrl limitUrlFromDb = (LimitUrl) iterator.next();
            LOG.info("mark limit url as deleted with id: " + limitUrlFromDb.getId());
            limitUrlFromDb.setDeleted(new Date());
            _limitUrlDao.makePersistent(limitUrlFromDb);
          }
          // disconnect form limit urls commands from DB
          for (LimitUrlCommand limitUrlCommand : getLimitUrlCommands()) {
            limitUrlCommand.setId(null);
          }
          
          Iterator<ExcludeUrl> itExcludeUrl = oldStartUrl.getExcludeUrls().iterator();
          while (itExcludeUrl.hasNext()) {
            ExcludeUrl excludeUrlFromDb = (ExcludeUrl) itExcludeUrl.next();
            LOG.info("mark exclude url as deleted with id: " + excludeUrlFromDb.getId());
            excludeUrlFromDb.setDeleted(new Date());
            _excludeUrlDao.makePersistent(excludeUrlFromDb);
          }
          for (ExcludeUrlCommand excludeUrlCommand : getExcludeUrlCommands()) {
            excludeUrlCommand.setId(null);
          }
          
          // mark the old start url as deleted
          oldStartUrl.setDeleted(new Date());
          _startUrlDao.makePersistent(oldStartUrl);

          // create a new start URL since the old one will be marked as deleted
          newStartUrl = new StartUrl();
          newStartUrl.setProvider(getProvider());
          
          newStartUrl.setUpdated(new Date());
      }
    } else {
      newStartUrl = new StartUrl();
      newStartUrl.setProvider(getProvider());
      newStartUrl.setUpdated(getUpdated());
    }
    
    newStartUrl.setUrl(getUrl());
    newStartUrl.setCreated(getCreated());

    handleLimitUrls(newStartUrl);
    handleExcludeUrls(newStartUrl);

    if (newStartUrl.getId() == null) {
      LOG.info("save new start url: " + newStartUrl);
      _startUrlDao.makePersistent(newStartUrl);
      _startUrlDao.flush();
    }
    return newStartUrl;

  }

  private void handleLimitUrls(StartUrl out) {
    Iterator<LimitUrl> iterator = out.getLimitUrls().iterator();
    while (iterator.hasNext()) {
      LimitUrl limitUrlFromDb = (LimitUrl) iterator.next();
      // skip deleted urls
      if (limitUrlFromDb.getDeleted() != null) {
          continue;
      }
      boolean delete = true;
      for (LimitUrlCommand limitUrlCommand : getLimitUrlCommands()) {
        // do not delete unchanged limit urls, but do mark renamed limit urls deleted
        if (limitUrlFromDb.getId().equals(limitUrlCommand.getId())) {
          if (limitUrlFromDb.getUrl().equals(limitUrlCommand.getUrl())) {
              // urls has not been changed
              delete = false;
              break;
          } else {
              // url has been renamed, disconnect limit URL from DB (and let the old url be marked as deleted)
              limitUrlCommand.setId(null);
              limitUrlCommand.setUpdated(new Date());
          }
        }
      }
      if (delete) {
        iterator.remove();
        LOG.info("mark limit url as deleted with id: " + limitUrlFromDb.getId());
        limitUrlFromDb.setDeleted(new Date());
        _limitUrlDao.makePersistent(limitUrlFromDb);
      }
    }

    for (LimitUrlCommand limitUrlCommand : getLimitUrlCommands()) {
      // add limit URL
      LimitUrl limitUrl = limitUrlCommand.write();
      // check for new limit URL
      if (limitUrl.getId() == null || !containsEntity(out.getLimitUrls(), limitUrl.getId())) {
        LOG.info("add new limit url: " + limitUrl);
        out.addLimitUrl(limitUrl);
      }
    }
  }

  private boolean containsEntity(List<? extends Url> urls, Long idToTest) {
    for (Url url : urls) {
      if (idToTest.equals(url.getId())) {
        return true;
      }
    }
    return false;
  }

  private void handleExcludeUrls(StartUrl out) {
    Iterator<ExcludeUrl> iterator = out.getExcludeUrls().iterator();
    while (iterator.hasNext()) {
      ExcludeUrl excludeUrlFromDb = (ExcludeUrl) iterator.next();
      // skip deleted urls
      if (excludeUrlFromDb.getDeleted() != null) {
          continue;
      }
      boolean delete = true;
      for (ExcludeUrlCommand excludeUrlCommand : getExcludeUrlCommands()) {
        // do not delete unchanged exclude urls, but do mark renamed exclude urls deleted
        if (excludeUrlFromDb.getId().equals(excludeUrlCommand.getId())) {
          if (excludeUrlFromDb.getUrl().equals(excludeUrlCommand.getUrl())) {
              // urls has not been changed
              delete = false;
              break;
          } else {
              // url has been renamed, disconnect limit URL from DB (and let the old url be marked as deleted)
              excludeUrlCommand.setId(null);
          }
        }
      }
      if (delete) {
        iterator.remove();
        LOG.info("mark exclude url as deleted with id: " + excludeUrlFromDb.getId());
        excludeUrlFromDb.setDeleted(new Date());
        _excludeUrlDao.makePersistent(excludeUrlFromDb);
      }
    }

    for (ExcludeUrlCommand excludeUrlCommand : getExcludeUrlCommands()) {
      // add exclude Url
      ExcludeUrl excludeUrl = excludeUrlCommand.write();
      // check for new exclude URL
      if (excludeUrl.getId() == null || !containsEntity(_excludeUrlCommands, excludeUrl.getId())) {
        LOG.info("add new exclude url: " + excludeUrl);
        out.addExcludeUrl(excludeUrl);
      }
    }
  }

}
