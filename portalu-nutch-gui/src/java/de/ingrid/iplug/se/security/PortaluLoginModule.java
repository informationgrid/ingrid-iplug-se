package de.ingrid.iplug.se.security;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
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
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

public class PortaluLoginModule extends AbstractLoginModule {

  private static final Log LOG = LogFactory.getLog(PortaluLoginModule.class);

  private static final String ROLE_PORTAL = "admin.portal";

  private static final String ROLE_PARTNER = "admin.portal.partner";

  private static final String ROLE_PROVIDER_INDEX = "admin.portal.partner.provider.index";

  private static final String ROLE_PROVIDER_CATALOG = "admin.portal.partner.provider.catalog";

  static class IndexIPlug extends HeartBeatPlug {

    private static IndexIPlug INSTANCE;

    public IndexIPlug() throws IOException {
      super(60000);
    }

    public static IndexIPlug getInstance() throws Exception {
      if (INSTANCE == null) {
        INSTANCE = new IndexIPlug();
        URL resource = PortaluLoginModule.class.getResource("/plugdescription-index.xml");
        File pd = new File(resource.getFile());
        PlugDescription plugDescription = new PlugdescriptionSerializer().deSerialize(pd);
        plugDescription.setRecordLoader(true);
        INSTANCE.configure(plugDescription);
        INSTANCE.startHeartBeats();
      }
      return INSTANCE;
    }

    @Override
    public IngridHits search(IngridQuery arg0, int arg1, int arg2) throws Exception {
      return null;
    }

    @Override
    public IngridHitDetail getDetail(IngridHit arg0, IngridQuery arg1, String[] arg2) throws Exception {
      return null;
    }

    @Override
    public IngridHitDetail[] getDetails(IngridHit[] arg0, IngridQuery arg1, String[] arg2) throws Exception {
      return null;
    }
  }

  @Override
  protected NutchGuiPrincipal authenticate(String userName, String password) {
    PortaluPrincipal principal = null;
    try {
      IndexIPlug plug = IndexIPlug.getInstance();
      URL communicationXml = PortaluLoginModule.class.getResource("/communication-index.xml");
      File communicationXmlFile = new File(communicationXml.getFile());
      IBus bus = BusClientFactory.createBusClient(communicationXmlFile, plug).getNonCacheableIBus();
      IngridHits authenticationData = login(bus, userName, password);
      IngridHit[] hits = authenticationData.getHits();
      if (isAuthenticated(hits)) {
        List<Map<String, Serializable>> allPartnerWithProvider = getAllPartnerWithProvider(bus);
        principal = createFromHits(userName, password, hits,
            allPartnerWithProvider);
      }
    } catch (Exception e) {
      LOG.error("error while authenticate against management iplug", e);
    }
    return principal;
  }

  private IngridHits login(IBus bus, String userName, Object credentials) {
    IngridHits result = new IngridHits(0, new IngridHit[] {});
    try {
      String digest = encode(userName, (String) credentials);
      IngridQuery ingridQuery = new IngridQuery();
      ingridQuery.addField(new FieldQuery(false, false, "datatype",
          "management"));
      ingridQuery.addField(new FieldQuery(false, false,
          "management_request_type", "0"));
      ingridQuery.addField(new FieldQuery(false, false, "login", userName));
      ingridQuery.addField(new FieldQuery(false, false, "digest", digest));
      result = bus.search(ingridQuery, 1000, 0, 0, 120000);
    } catch (Exception e) {
      LOG.error("error while bus searching", e);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private PortaluPrincipal createFromHits(String userName, String password,
      IngridHit[] hits, List<Map<String, Serializable>> allPartnerWithProvider) {
    Set<String> roles = new HashSet<String>();
    Set<String> partners = new HashSet<String>();
    Set<String> providers = new HashSet<String>();
    for (int i = 0; i < hits.length; i++) {
      IngridHit hit = hits[i];

      String permission = (String) hit.get("permission");
      roles.add(permission);
      if (permission.equals(ROLE_PORTAL)) {
        partners.add("*");
        providers.add("*");
      } else if (permission.equals(ROLE_PARTNER)) {
        String[] partnerArray = (String[]) hit.getArray("partner");
        partners.addAll(Arrays.asList(partnerArray));
        providers.add("*");
      } else if (permission.equals(ROLE_PROVIDER_CATALOG)
          || permission.equals(ROLE_PROVIDER_INDEX)) {
        String[] partnerArray = (String[]) hit.getArray("partner");
        partners.addAll(Arrays.asList(partnerArray));

        String[] providerArray = (String[]) hit.getArray("provider");
        providers.addAll(Arrays.asList(providerArray));
      }
    }

    Iterator<Map<String, Serializable>> partnerIterator = allPartnerWithProvider
        .iterator();
    while (!partners.contains("*") && partnerIterator.hasNext()) {
      Map<java.lang.String, java.io.Serializable> partnerMap = (Map<java.lang.String, java.io.Serializable>) partnerIterator
          .next();
      String partnerId = (String) partnerMap.get("partnerid");
      if (!partners.contains(partnerId)) {
        LOG.info("partner is not assigned, remove it: " + partnerId);
        partnerIterator.remove();
        continue;
      }
      List<Map<String, Serializable>> allProviders = (List<Map<String, Serializable>>) partnerMap
          .get("providers");
      Iterator<Map<String, Serializable>> providerIterator = allProviders
          .iterator();
      while (!providers.contains("*") && providerIterator.hasNext()) {
        Map<java.lang.String, java.io.Serializable> providerMap = (Map<java.lang.String, java.io.Serializable>) providerIterator
            .next();
        String providerId = (String) providerMap.get("providerid");
        if (!providers.contains(providerId)) {
          LOG.info("provider is not assigned, remove it: " + providerId);
          providerIterator.remove();
        }
      }
    }

    LOG.info("the following partner/provider are assigned to user [" + userName
        + "] with roles [" + roles + "]");
    LOG.info(allPartnerWithProvider);

    return new PortaluPrincipal(userName, password, roles,
        allPartnerWithProvider);
  }

  private String encode(String userName, String clearTextPassword)
      throws NoSuchAlgorithmException {
    byte value[];
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    messageDigest.reset();
    value = messageDigest.digest(clearTextPassword.getBytes());
    messageDigest.update(userName.getBytes());
    value = messageDigest.digest(value);
    return new String(new Base64().encode(value));
  }

  private boolean isAuthenticated(IngridHit[] hits) {
    return hits.length > 0 ? hits[0].getBoolean("authenticated") : false;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Serializable>> getAllPartnerWithProvider(IBus bus) {
  
    List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
    try {
      String query = "datatype:management management_request_type:1";
      IngridQuery ingridQuery = QueryStringParser.parse(query);
      IngridHits hits = bus.search(ingridQuery, 1, 1, 1, 1000);
      if (hits.length() > 0) {
        IngridHit hit = hits.getHits()[0];
        list = hit.getArrayList("partner");
      }
    } catch (Exception e) {
      LOG.error("can not send query to bus.", e);
    }
    return list;
  }

}
