package de.ingrid.iplug.se.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.Credential;
import org.mortbay.jetty.security.Password;
import org.mortbay.jetty.security.SSORealm;
import org.mortbay.jetty.security.UserRealm;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.nutch.admin.ConfigurationUtil;
import de.ingrid.nutch.admin.security.JUserJPasswordCallbackHandler;
import de.ingrid.nutch.admin.security.NutchGuiPrincipal;
import de.ingrid.nutch.admin.security.NutchGuiPrincipal.KnownPrincipal;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class ShibbolethRealm implements UserRealm, SSORealm {

    class SSOToken {
        private final Principal _principal;

        private final Credential _credential;

        public SSOToken(Principal principal, Credential credential) {
            _principal = principal;
            _credential = credential;
        }

        @Override
        public String toString() {
            return _principal.getName();
        }
    }

    private static final String ROLE_ADMIN = "admin";

    private static final String ROLE_PORTAL = "admin.portal";

    private static final String ROLE_PARTNER = "admin.portal.partner";

    private static final String ROLE_PROVIDER_INDEX = "admin.portal.partner.provider.index";

    private static final String ROLE_PROVIDER_CATALOG = "admin.portal.partner.provider.catalog";

    private static final String ROLE_CONFIGURATOR = "admin.portal.configurator";

    private final Log LOG = LogFactory.getLog(ShibbolethRealm.class);

    private Map<String, SSOToken> _ssoMap = new HashMap<String, SSOToken>();

    private final boolean _securityEnabled;
    
    private String _headerName = null;

    public ShibbolethRealm(boolean secure) {
        _securityEnabled = secure;
    }

    @Override
    public void clearSingleSignOn(String userName) {
        Iterator<String> iterator = _ssoMap.keySet().iterator();
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            SSOToken ssoToken = _ssoMap.get(id);
            if (ssoToken._principal.getName().equals(userName)) {
                LOG.info("remove sso token for id: " + id + " and user name:" + userName);
                iterator.remove();
            }
        }
        LOG.info("sso tokens in memory: " + _ssoMap);
    }

    @Override
    public Credential getSingleSignOn(Request request, Response response) {
        Credential credential = null;
        Principal principal = null;
        
        if (this._headerName == null)
            setHeaderName(request);
        
        String userName = request.getHeader(this._headerName);

        String id = generateId(request);
        LOG.info("try to load sso token with id: " + id);

        if (_ssoMap.containsKey(id)) {
            SSOToken ssoToken = _ssoMap.get(id);
            principal = ssoToken._principal;
            LOG.info("found principal: " + principal);
            if (request.getUserRealm().reauthenticate(principal)) {
                request.setUserPrincipal(principal);
//                request.setAuthUser(principal.getName());
                credential = ssoToken._credential;
            } else {
                _ssoMap.remove(id);
            }
        } else {
            // no shibboleth header with user information
            if (userName == null) {
                if (LOG.isDebugEnabled()) {
                    String header = "";
                    Enumeration headerNames =  request.getHeaderNames();
                    while (headerNames.hasMoreElements()) 
                        header += headerNames.nextElement() + ";";

                    LOG.debug("RequestHeaderNames: " + header);
                }
                return null;
            }

            // request Management iPlug to get User Roles
            final IBus bus = BusClientFactory.getBusClient().getNonCacheableIBus();
            final IngridHits authenticationData = getRolesFromPortal(bus, userName);
            final IngridHit[] hits = authenticationData.getHits();
            if (hits.length > 0) {
                final List<Map<String, Serializable>> allPartnerWithProvider = getAllPartnerWithProvider(bus);
                principal = createFromHits(userName, hits, allPartnerWithProvider);
                // principal = new
                // de.ingrid.iplug.se.security.PortaluPrincipal(userName,
                // "ignore", "admin", "admin.portal");
                request.setUserPrincipal(principal);
//                request.setAuthUser(principal.getName());
                credential = new Password("ignore");

                setSingleSignOn(request, response, principal, credential);
            } else {
                LOG.warn("User might not exist in Portal! No roles found for user: " + userName);
            }
        }
        
        if (LOG.isDebugEnabled()) {
            if (credential != null)
                LOG.debug("found credential for session-id: " + id);
            else
                LOG.debug("did not found credential for session-id: " + id);
        }

        return credential;
    }

    private void setHeaderName(Request request) {
        ConfigurationUtil configurationUtil = (ConfigurationUtil) request.getSession().getServletContext().getAttribute("configurationUtil");
        try {
            Configuration conf = configurationUtil.loadConfiguration("general");
            this._headerName = conf.get("shib_header_name");
            if ( this._headerName == null) {
                this._headerName = "NOT_CONFIGURED";
                LOG.debug("Shibboleth not configured in general/nutch-site.xml ... searched for 'shib_header_name'");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void setSingleSignOn(Request request, Response response, Principal principal, Credential credential) {
        Set<String> keySet = _ssoMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            SSOToken ssoToken = _ssoMap.get(id);
            if (ssoToken._principal.getName().equals(principal.getName())) {
                LOG.info("remove sso token [" + id + "] for user [" + principal.getName() + "].");
                iterator.remove();
            }
        }
        String id = generateId(request);
        LOG.info("create new sso token for id: " + id);
        _ssoMap.put(id, new SSOToken(principal, credential));
        LOG.info("sso tokens in memory: " + _ssoMap);

    }

    @Override
    public Principal authenticate(String userName, Object password, Request request) {
        Principal principal = new NutchGuiPrincipal.SuperAdmin("Admin");
        if (_securityEnabled) {
            principal = null;
            try {
                JUserJPasswordCallbackHandler handler = new JUserJPasswordCallbackHandler(request);
                LoginContext loginContext = new LoginContext("NutchGuiLogin", handler);
                loginContext.login();
                Subject subject = loginContext.getSubject();
                Set<Principal> principals = subject.getPrincipals();
                Principal tmpPrincipal = principals.isEmpty() ? principal : principals.iterator().next();
                if (tmpPrincipal instanceof KnownPrincipal) {
                    KnownPrincipal knownPrincipal = (KnownPrincipal) tmpPrincipal;
                    knownPrincipal.setLoginContext(loginContext);
                    principal = knownPrincipal;
                    LOG.info("principal has logged in: " + principal);
                }
            } catch (LoginException e) {
                LOG.error("login error for user: " + userName);
            }
        }
        if (principal == null) {
            LOG.info("login failed for userName: " + userName);
        }
        return principal;
    }

    @Override
    public void disassociate(Principal arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
        return ShibbolethRealm.class.getSimpleName(); // creates
                    // configuration error if more than one Realm is present!?
    }

    @Override
    public Principal getPrincipal(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isUserInRole(Principal principal, String role) {
        boolean bit = false;
        if (principal instanceof KnownPrincipal) {
            KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
            bit = knownPrincipal.isInRole(role);
        }
        return bit;
    }

    @Override
    public void logout(Principal principal) {
        try {
            if (principal instanceof KnownPrincipal) {
                KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
                LoginContext loginContext = knownPrincipal.getLoginContext();
                if (loginContext != null) {
                    loginContext.logout();
                }
                LOG.info("principal has logged out: " + knownPrincipal);
            }
        } catch (LoginException e) {
            LOG.warn("logout failed", e);
        }
    }

    @Override
    public Principal popRole(Principal principal) {
        // not necessary
        return principal;
    }

    @Override
    public Principal pushRole(Principal principal, String role) {
        // not necessary
        return principal;
    }

    @Override
    public boolean reauthenticate(Principal principal) {
        return (principal instanceof KnownPrincipal);
    }

    private String generateId(Request request) {
        return request.getSession().getId();
    }

    private IngridHits getRolesFromPortal(final IBus bus, final String userName) {
        IngridHits result = new IngridHits(0, new IngridHit[] {});
        try {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "0"));
            ingridQuery.addField(new FieldQuery(false, false, "login", userName));
            ingridQuery.addField(new FieldQuery(false, false, "onlyRoles", "true"));
            result = bus.search(ingridQuery, 1000, 0, 0, 120000);
        } catch (final Exception e) {
            LOG.error("error while bus searching", e);
        }

        return result;
    }

    private boolean isAuthenticated(final IngridHit[] hits) {
        return hits.length > 0 ? hits[0].getBoolean("authenticated") : false;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Serializable>> getAllPartnerWithProvider(final IBus bus) {

        List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
        try {
            final String query = "datatype:management management_request_type:1";
            final IngridQuery ingridQuery = QueryStringParser.parse(query);
            final IngridHits hits = bus.search(ingridQuery, 1, 1, 1, 1000);
            if (hits.length() > 0) {
                final IngridHit hit = hits.getHits()[0];
                list = hit.getArrayList("partner");
            }
        } catch (final Exception e) {
            LOG.error("can not send query to bus.", e);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private PortaluPrincipal createFromHits(final String userName, final IngridHit[] hits,
            final List<Map<String, Serializable>> allPartnerWithProvider) {
        final Set<String> roles = new HashSet<String>();
        final Set<String> partners = new HashSet<String>();
        final Set<String> providers = new HashSet<String>();
        for (final IngridHit hit : hits) {
            final String permission = (String) hit.get("permission");
            if (permission == null)
                continue;

            roles.add(permission);
            if (permission.equals(ROLE_PORTAL) || permission.equals(ROLE_ADMIN)) {
                partners.add("*");
                providers.add("*");
            } else if (permission.equals(ROLE_PARTNER)) {
                final String[] partnerArray = (String[]) hit.getArray("partner");
                partners.addAll(Arrays.asList(partnerArray));
                providers.add("*");
            } else if (permission.equals(ROLE_PROVIDER_CATALOG) || permission.equals(ROLE_PROVIDER_INDEX)) {
                final String[] partnerArray = (String[]) hit.getArray("partner");
                partners.addAll(Arrays.asList(partnerArray));

                final String[] providerArray = (String[]) hit.getArray("provider");
                providers.addAll(Arrays.asList(providerArray));
            }
        }

        final Iterator<Map<String, Serializable>> partnerIterator = allPartnerWithProvider.iterator();
        while (!partners.contains("*") && partnerIterator.hasNext()) {
            final Map<String, Serializable> partnerMap = partnerIterator.next();
            final String partnerId = (String) partnerMap.get("partnerid");
            if (!partners.contains(partnerId)) {
                LOG.info("partner is not assigned, remove it: " + partnerId);
                partnerIterator.remove();
                continue;
            }
            final List<Map<String, Serializable>> allProviders = (List<Map<String, Serializable>>) partnerMap
                    .get("providers");
            final Iterator<Map<String, Serializable>> providerIterator = allProviders.iterator();
            while (!providers.contains("*") && providerIterator.hasNext()) {
                final Map<String, Serializable> providerMap = providerIterator.next();
                final String providerId = (String) providerMap.get("providerid");
                if (!providers.contains(providerId)) {
                    LOG.info("provider is not assigned, remove it: " + providerId);
                    providerIterator.remove();
                }
            }
        }

        LOG.info("the following partner/provider are assigned to user [" + userName + "] with roles [" + roles + "]");
        LOG.info(allPartnerWithProvider);

        return new PortaluPrincipal(userName, "ignore", roles, allPartnerWithProvider);
    }

}
