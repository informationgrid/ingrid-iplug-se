package de.ingrid.iplug.se.urlmaintenance;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public class PartnerProviderCommand {

  private Partner _partner;
  private Provider _provider;

  public Partner getPartner() {
    return _partner;
  }

  public void setPartner(Partner partner) {
    _partner = partner;
  }

  public Provider getProvider() {
    return _provider;
  }

  public void setProvider(Provider provider) {
    _provider = provider;
  }

}
