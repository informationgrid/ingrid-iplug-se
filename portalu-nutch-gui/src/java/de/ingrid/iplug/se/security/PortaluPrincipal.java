package de.ingrid.iplug.se.security;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.nutch.admin.security.NutchGuiPrincipal.KnownPrincipal;

public class PortaluPrincipal extends KnownPrincipal {

  private final List<Map<String, Serializable>> _allPartnerWithProvider;

  public PortaluPrincipal(String name, String password, Set<String> roles,
      List<Map<String, Serializable>> allPartnerWithProvider) {
    super(name, password, roles);
    _allPartnerWithProvider = allPartnerWithProvider;
  }

  public List<Map<String, Serializable>> getAllPartnerWithProvider() {
    return _allPartnerWithProvider;
  }

}
