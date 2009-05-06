package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Partner extends IdBase {

  private String _name;

  @OneToMany
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

}
