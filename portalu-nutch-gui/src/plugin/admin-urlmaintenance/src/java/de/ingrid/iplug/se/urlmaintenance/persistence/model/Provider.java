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
@NamedQueries(value = { @NamedQuery(name = "getProviderByName", query = "select p from Provider as p where p._name = :name"),
    @NamedQuery(name = "getProviderByNameAndPartner", query = "select p from Provider as p where p._name = :name and p._partner = :partner") })
public class Provider extends IdBase {

  @Column(nullable = false)
  private String _name;
  
  @Column(nullable = false)
  private String _shortName;

  // rwe: The annotation @JoinColumn{nullable="true"} causes intermittent
  // failures when using the eclipselink JPA!
  // Sometimes the eclipselink wants to update the provider table to set an
  // partner_fk value to 'null'. This is very curious and makes the system not
  // deterministic. I've seen the effect in PartnerDao.removeProvider() method,
  // which sometimes works and sometimes it causes an sql-exception.
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
  @JoinColumn(/* nullable = false, */name = "partner_fk")
  private Partner _partner;

  @OneToMany
  @JoinColumn(name = "provider_fk")
  private List<Url> _urls = new ArrayList<Url>();

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public Partner getPartner() {
    return _partner;
  }

  public void setPartner(Partner partner) {
    _partner = partner;
  }

  public List<Url> getUrls() {
    return _urls;
  }

  public void setUrls(List<Url> urls) {
    _urls = urls;
  }

  public void setShortName(String shortName) {
    _shortName = shortName;
  }

  public String getShortName() {
    return _shortName;
  }

}
