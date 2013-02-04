package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@Cacheable(false)
@DiscriminatorValue("START")
@NamedQueries(value = {
    @NamedQuery(name = "getAllUrlsByProviderOrderByCreatedAsc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.created asc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByCreatedDesc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.created desc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUpdatedAsc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.updated asc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUpdatedDesc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.updated desc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUrlAsc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.url asc"),
    @NamedQuery(name = "getAllUrlsByProviderOrderByUrlDesc", query = "select u from StartUrl as u where u.deleted is NULL and u.provider.id = :id order by u.url desc"),
    @NamedQuery(name = "countByProvider", query = "select count(u) from StartUrl as u where u.deleted is NULL and u.provider.id = :id") })
public class StartUrl extends WebUrl {

  @OneToMany
  @JoinColumn(name = "startUrl_fk")
  private List<LimitUrl> limitUrls = new ArrayList<LimitUrl>();

  @OneToMany
  @JoinColumn(name = "startUrl_fk")
  private List<ExcludeUrl> excludeUrls = new ArrayList<ExcludeUrl>();

  public List<LimitUrl> getLimitUrls() {
    return limitUrls;
  }

  public void setLimitUrls(List<LimitUrl> limitUrls) {
    this.limitUrls = limitUrls;
  }

  public List<ExcludeUrl> getExcludeUrls() {
    return excludeUrls;
  }

  public void setExcludeUrls(List<ExcludeUrl> excludeUrls) {
    this.excludeUrls = excludeUrls;
  }

  public void addLimitUrl(LimitUrl limitUrl) {
    limitUrls.add(limitUrl);
  }

  public void addExcludeUrl(ExcludeUrl excludeUrl) {
    excludeUrls.add(excludeUrl);
  }

}
