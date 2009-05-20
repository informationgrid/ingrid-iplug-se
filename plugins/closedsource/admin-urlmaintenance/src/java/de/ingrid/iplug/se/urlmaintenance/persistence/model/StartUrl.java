package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("START")
@NamedQueries(value = {
    @NamedQuery(name = "getAllUrlsByProviderOrderByTimeStampAsc", query = "select u from StartUrl as u where u._provider._id = :id order by u._timeStamp asc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByTimeStampDesc", query = "select u from StartUrl as u where u._provider._id = :id order by u._timeStamp desc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUrlAsc", query = "select u from StartUrl as u where u._provider._id = :id order by u._url asc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUrlDesc", query = "select u from StartUrl as u where u._provider._id = :id order by u._url desc"),
    @NamedQuery(name = "countByProvider", query = "select count(u) from StartUrl as u where u._provider._id = :id") })
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
