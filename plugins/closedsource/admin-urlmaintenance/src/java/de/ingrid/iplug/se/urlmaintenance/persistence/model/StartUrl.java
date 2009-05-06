package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("START")
public class StartUrl extends WebUrl {

  @OneToMany
  @JoinColumn(name = "startUrl_fk")
  private List<LimitUrl> _limitUrls = new ArrayList<LimitUrl>();

  @OneToMany
  @JoinColumn(name = "startUrl_fk")
  private List<ExcludeUrl> _excludeUrls = new ArrayList<ExcludeUrl>();

  public List<LimitUrl> getLimitUrls() {
    return _limitUrls;
  }

  public void setLimitUrls(List<LimitUrl> limitUrls) {
    _limitUrls = limitUrls;
  }

  public List<ExcludeUrl> getExcludeUrls() {
    return _excludeUrls;
  }

  public void setExcludeUrls(List<ExcludeUrl> excludeUrls) {
    _excludeUrls = excludeUrls;
  }

  public void addLimitUrl(LimitUrl limitUrl) {
    _limitUrls.add(limitUrl);
  }

  public void addExcludeUrl(ExcludeUrl excludeUrl) {
    _excludeUrls.add(excludeUrl);
  }

}
