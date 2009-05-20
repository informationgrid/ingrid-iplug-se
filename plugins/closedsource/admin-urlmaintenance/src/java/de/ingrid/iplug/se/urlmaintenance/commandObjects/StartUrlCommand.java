package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public class StartUrlCommand extends StartUrl {

  private Long _id;

  private List<LimitUrlCommand> _limitUrlCommands = new ArrayList<LimitUrlCommand>();

  private List<ExcludeUrlCommand> _excludeUrlCommands = new ArrayList<ExcludeUrlCommand>();

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

}
