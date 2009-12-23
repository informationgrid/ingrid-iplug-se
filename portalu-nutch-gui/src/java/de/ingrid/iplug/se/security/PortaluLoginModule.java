package de.ingrid.iplug.se.security;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.security.AbstractLoginModule;
import org.apache.nutch.admin.security.NutchGuiPrincipal;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class PortaluLoginModule extends AbstractLoginModule {

    private static final Log LOG = LogFactory.getLog(PortaluLoginModule.class);

    private static final String ROLE_PORTAL = "admin.portal";

    private static final String ROLE_PARTNER = "admin.portal.partner";

    private static final String ROLE_PROVIDER_INDEX = "admin.portal.partner.provider.index";

    private static final String ROLE_PROVIDER_CATALOG = "admin.portal.partner.provider.catalog";

    private static final String ROLE_CONFIGURATOR = "admin.portal.configurator";

    @Override
    protected NutchGuiPrincipal authenticate(final String userName, final String password) {
        final String pd = System.getProperty("plugDescription");
        final File file = new File(pd);
        NutchGuiPrincipal principal = null;
        if (file.exists()) {
            try {
                final IBus bus = BusClientFactory.getBusClient().getNonCacheableIBus();
                final IngridHits authenticationData = login(bus, userName, password);
                final IngridHit[] hits = authenticationData.getHits();
                if (isAuthenticated(hits)) {
                    final List<Map<String, Serializable>> allPartnerWithProvider = getAllPartnerWithProvider(bus);
                    principal = createFromHits(userName, password, hits, allPartnerWithProvider);
                }
            } catch (final Exception e) {
                LOG.error("error while authenticate against management iplug", e);
            }
        } else {
            principal = new PortaluPrincipal("configurator", "configurator", ROLE_CONFIGURATOR);
        }
        return principal;
    }

    private IngridHits login(final IBus bus, final String userName, final Object credentials) {
        IngridHits result = new IngridHits(0, new IngridHit[] {});
        try {
            final String digest = encode(userName, (String) credentials);
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "0"));
            ingridQuery.addField(new FieldQuery(false, false, "login", userName));
            ingridQuery.addField(new FieldQuery(false, false, "digest", digest));
            result = bus.search(ingridQuery, 1000, 0, 0, 120000);
        } catch (final Exception e) {
            LOG.error("error while bus searching", e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private PortaluPrincipal createFromHits(final String userName, final String password, final IngridHit[] hits,
            final List<Map<String, Serializable>> allPartnerWithProvider) {
        final Set<String> roles = new HashSet<String>();
        final Set<String> partners = new HashSet<String>();
        final Set<String> providers = new HashSet<String>();
        for (final IngridHit hit : hits) {
            final String permission = (String) hit.get("permission");
            roles.add(permission);
            if (permission.equals(ROLE_PORTAL)) {
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

        return new PortaluPrincipal(userName, password, roles, allPartnerWithProvider);
    }

    private String encode(final String userName, final String clearTextPassword) throws NoSuchAlgorithmException {
        byte value[];
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.reset();
        value = messageDigest.digest(clearTextPassword.getBytes());
        messageDigest.update(userName.getBytes());
        value = messageDigest.digest(value);
        return new String(new Base64().encode(value));
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

}
