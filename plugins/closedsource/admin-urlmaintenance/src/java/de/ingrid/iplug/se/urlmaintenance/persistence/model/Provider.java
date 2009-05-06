package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Provider extends IdBase {

  private String _name;

  @ManyToOne
  @JoinColumn(name = "partner_fk")
  private Partner _partner;

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

}
