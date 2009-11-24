package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQuery(name = "getPartnerByName", query = "select p from Partner as p where p._name = :name")
public class Partner extends IdBase {

  // rwe: Heads-up: @Column(unique=true) does not work with eclipselink and
  // HSQLDB!
  @Column(nullable = false/* , unique=true */)
  private String _name;

  @OneToMany(cascade = { CascadeType.ALL })
  @JoinColumn(name = "partner_fk")
  private List<Provider> _providers = new ArrayList<Provider>();

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public List<Provider> getProviders() {
    return _providers;
  }

  public void setProviders(List<Provider> providers) {
    _providers = providers;
  }

  public void addProvider(Provider provider) {
    _providers.add(provider);
    provider.setPartner(this);
  }
}
