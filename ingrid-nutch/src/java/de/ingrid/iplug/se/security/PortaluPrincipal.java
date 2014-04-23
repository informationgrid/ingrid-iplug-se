package de.ingrid.iplug.se.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.nutch.admin.security.NutchGuiPrincipal.KnownPrincipal;

public class PortaluPrincipal extends KnownPrincipal {

  private final List<Map<String, Serializable>> _allPartnerWithProvider;

  public PortaluPrincipal(final String name, final String password, final Set<String> roles,
      final List<Map<String, Serializable>> allPartnerWithProvider) {
    super(name, password, roles);
    _allPartnerWithProvider = allPartnerWithProvider;
  }

    public PortaluPrincipal(final String name, final String password, final String... roles) {
        super(name, password, new HashSet<String>());
        final Set<String> set = getRoles();
        for (final String role : roles) {
            set.add(role);
        }
        _allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    }

  public List<Map<String, Serializable>> getAllPartnerWithProvider() {
    return _allPartnerWithProvider;
  }

}
