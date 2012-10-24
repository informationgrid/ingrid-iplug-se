package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries(value = { 
        @NamedQuery(name = "getProviderByName", query = "select p from Provider as p where p.name = :name"),
        @NamedQuery(name = "getProviderByShortName", query = "select p from Provider as p where p.shortName = :shortName"),
        @NamedQuery(name = "getProviderByNameAndPartner", query = "select p from Provider as p where p.name = :name and p.partner = :partner"), 
        @NamedQuery(name = "getProviderByShortNameNameAndPartner", query = "select p from Provider as p where p.shortName = :shortName and p.partner = :partner") 
        })
public class Provider extends IdBase {

  @Column(nullable = false)
  private String name;
  
  @Column(nullable = false)
  private String shortName;

  // rwe: The annotation @JoinColumn{nullable="true"} causes intermittent
  // failures when using the eclipselink JPA!
  // Sometimes the eclipselink wants to update the provider table to set an
  // partner_fk value to 'null'. This is very curious and makes the system not
  // deterministic. I've seen the effect in PartnerDao.removeProvider() method,
  // which sometimes works and sometimes it causes an sql-exception.
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
  @JoinColumn(/* nullable = false, */name = "partner_fk")
  private Partner partner;

  @OneToMany
  @JoinColumn(name = "provider_fk")
  private List<Url> urls = new ArrayList<Url>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Partner getPartner() {
    return partner;
  }

  public void setPartner(Partner partner) {
    this.partner = partner;
  }

  public List<Url> getUrls() {
    return urls;
  }

  public void setUrls(List<Url> urls) {
    this.urls = urls;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getShortName() {
    return shortName;
  }

}
