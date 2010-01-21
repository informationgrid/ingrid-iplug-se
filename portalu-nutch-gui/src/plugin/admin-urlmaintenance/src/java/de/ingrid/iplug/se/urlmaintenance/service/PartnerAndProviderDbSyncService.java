package de.ingrid.iplug.se.urlmaintenance.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Service
public class PartnerAndProviderDbSyncService {

  private final Log LOG = LogFactory.getLog(PartnerAndProviderDbSyncService.class);

  private final IPartnerDao _partnerDao;
  private final IUrlDao _urlDao;

  private class InternalPartner {
    private String _id;
    private String _name;
    private Set<InternalProvider> _providers = new HashSet<InternalProvider>();

    public String getId() {
      return _id;
    }

    public void setId(String id) {
      this._id = id;
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      this._name = name;
    }

    public Set<InternalProvider> getProviders() {
      return _providers;
    }

    public void addProviders(InternalProvider provider) {
      _providers.add(provider);
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  };

  private class InternalProvider {

    private String _id;
    private String _name;
    private List<String> _urls = new ArrayList<String>();

    @SuppressWarnings("unused")
    public String getId() {
      return _id;
    }

    public void setId(String id) {
      _id = id;
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    @SuppressWarnings("unused")
    public List<String> getUrls() {
      return _urls;
    }

    public void addUrl(String url) {
      _urls.add(url);
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  };

  @Autowired
  public PartnerAndProviderDbSyncService(IPartnerDao partnerDao, IUrlDao urlDao) {
    super();
    _partnerDao = partnerDao;
    _urlDao = urlDao;
  }

  public void syncDb(List<Map<String, Serializable>> allPartnerWithProvider) {

    // Parse input param 'allPartnerWithProvider' into a more handy list of
    // objects.
    List<InternalPartner> partnersAndProviders = parse(allPartnerWithProvider);
    Queue<Partner> partnersInDb = new LinkedList<Partner>(_partnerDao.getAll());
    Partner partnerInDb = partnersInDb.poll();
    while (partnerInDb != null) {
      InternalPartner partnerWithProvider = findInternalPartnerByPartnerName(partnersAndProviders, partnerInDb
          .getName());

      if (partnerWithProvider != null) {
        // there exists the same partner from ibus with partner stored in db
        syncProviders(partnerInDb, partnerWithProvider.getProviders());
        // remove partner from temporary ibus list
        partnersAndProviders.remove(partnerWithProvider);
      } else {
        // The partner stored in db exists not on ibus results.
        // But before we remove it we have to test if the partner's providers
        // are not referred by any URL.
        if (_urlDao.countByProvider(IdBase.toIds(partnerInDb.getProviders())) == 0L) {
          LOG.info("Remove partner with name: '" + partnerInDb.getName() + "'.");
          _partnerDao.makeTransient(partnerInDb);
        } else {
          LOG.info("Can not remove partner with name: '" + partnerInDb.getName()
              + "', because it is already used by an URL.");
        }
      }

      partnerInDb = partnersInDb.poll();
    }

    // add addional partner and its provides to db
    for (InternalPartner partnerWitProvider : partnersAndProviders) {
      createPartnerAndProviders(partnerWitProvider);
    }
    _partnerDao.flipTransaction();
  }

  private Partner createPartnerAndProviders(InternalPartner internalPartner) {
    Partner newPartner = new Partner();
    newPartner.setShortName(internalPartner.getId());
    newPartner.setName(internalPartner.getName());
    LOG.info("Create new partner with name '" + internalPartner.getName() + "'.");
    for (InternalProvider provider : internalPartner.getProviders()) {
      LOG.info("Add new provider with name '" + provider.getName() + "' to existing partner '"
          + internalPartner.getName() + "'.");
      Provider newProvider = craeteProvider(provider);
      newPartner.addProvider(newProvider);
    }
    _partnerDao.makePersistent(newPartner);
    return newPartner;
  }

  private Provider craeteProvider(InternalProvider provider) {
    Provider newProvider = new Provider();
    newProvider.setShortName(provider.getId());
    newProvider.setName(provider.getName());

    return newProvider;
  }

  private void syncProviders(Partner partnerInDb, Set<InternalProvider> providers) {
    Set<String> providersToAdd = new HashSet<String>();
    Set<Provider> providersToRemove = new HashSet<Provider>();
    for (InternalProvider internalProvider : providers) {
      providersToAdd.add(internalProvider.getName());
    }

    for (Provider provider : partnerInDb.getProviders()) {
      if (providersToAdd.contains(provider.getName())) {
        // the partner in database already contains wanted provider
        providersToAdd.remove(provider.getName());
      } else {
        // the partner in database contains a provider that is no longer wanted
        providersToRemove.add(provider);
      }
    }

    // remove provider from partner?
    for (Provider provider : providersToRemove) {
      LOG.info("Remove provider '" + provider.getName() + "' from partner '" + partnerInDb.getName() + "'.");
      _partnerDao.removeProvider(partnerInDb, provider);
    }
    // if (providersToRemove.size() > 0) {
    // _partnerDao.flipTransaction();
    // }

    // add provider for partner?
    for (String providerName : providersToAdd) {
      LOG.info("Add new provider '" + providerName + "' to existing partner '" + partnerInDb.getName() + "'.");
      Provider newProvider = craeteProvider(findInternalProviderByName(providers, providerName));
      partnerInDb.addProvider(newProvider);
    }
    if (providersToAdd.size() > 0) {
      _partnerDao.makePersistent(partnerInDb);
      // _partnerDao.flipTransaction();
    }
  }

  private InternalProvider findInternalProviderByName(Set<InternalProvider> providers, String providerName) {
    for (InternalProvider internalProvider : providers) {
      if (internalProvider.getName().equals(providerName)) {
        return internalProvider;
      }
    }
    throw new RuntimeException("Can not fine an Internal Provider form name '" + providerName + "' from list: "
        + providers);
  }

  private static InternalPartner findInternalPartnerByPartnerName(List<InternalPartner> partnersAndProviders,
      String searchPartnerName) {
    for (InternalPartner partnerWithProvider : partnersAndProviders) {
      if (searchPartnerName.equals(partnerWithProvider.getName())) {
        return partnerWithProvider;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<InternalPartner> parse(List<Map<String, Serializable>> allPartnerWithProvider) {
    // The expected structure in input param is:
    // key : partnerid
    // value: (java.lang.String) bund
    // key : providers
    // value: (java.util.ArrayList) [{providerid=bu_bmbf, name=Bundesministerium
    // für Bildung und Forschung, url=}, {providerid=bu_atest, name=Test1,
    // url=http://www.kst.portalu.de}, {providerid=bu_bmu,
    // name=Bundesministerium für Umwelt, Naturschutz und Reaktorsicherheit,
    // url=http://www.bmu.de/}, {providerid=bu_uba, name=Umweltbundesamt,
    // url=http://www.umweltbundesamt.de/}, {providerid=bu_bfn, name=Bundesamt
    // für Naturschutz, url=http://www.bfn.de/}, {providerid=bu_bfs,
    // name=Bundesamt für Strahlenschutz, url=http://www.bfs.de/},
    // {providerid=bu_bmf, name=Bundesministerium der Finanzen,
    // url=http://www.bundesfinanzministerium.de/}, {providerid=bu_bmelv,
    // name=Bundesministerium für Ernährung, Landwirtschaft und
    // Verbraucherschutz,
    // url=http://www.bmelv.de/cln_044/DE/00-Home/__Homepage__node.html__nnn=true},
    // {providerid=bu_bmz, name=Bundesministerium für wirtschaftliche
    // Zusammenarbeit und Entwicklung, url=http://www.bmz.de/},
    // {providerid=bu_aa, name=Auswärtiges Amt,
    // url=http://www.auswaertiges-amt.de/}, {providerid=bu_bsh, name=Bundesamt
    // für Seeschifffahrt und Hydrographie, url=http://www.bsh.de/},
    // {providerid=bu_bvl, name=Bundesamt für Verbraucherschutz und
    // Lebensmittelsicherheit, url=http://www.bvl.bund.de/}, {providerid=bu_bgr,
    // name=Bundesanstalt für Geowissenschaften und Rohstoffe,
    // url=http://www.bgr.bund.de/}, {providerid=bu_bfg, name=Bundesanstalt für
    // Gewässerkunde, url=http://www.bafg.de/}, {providerid=bu_nokis,
    // name=Bundesanstalt für Wasserbau - Dienststelle Hamburg,
    // url=http://www.hamburg.baw.de/}, {providerid=bu_bfr, name=Bundesinstitut
    // für Risikobewertung, url=http://www.bfr.bund.de/}, {providerid=bu_bka,
    // name=Bundeskriminalamt, url=http://www.bka.de/}, {providerid=bu_rki,
    // name=Robert-Koch-Institut, url=http://www.rki.de/}, {providerid=bu_stba,
    // name=Statistisches Bundesamt, url=http://www.destatis.de/},
    // {providerid=bu_ble, name=Bundesanstalt für Landwirtschaft und Ernährung,
    // url=http://www.ble.de}, {providerid=bu_bpb, name=Bundeszentrale für
    // politische Bildung, url=http://www.bpb.de/}, {providerid=bu_gtz,
    // name=Deutsche Gesellschaft für Technische Zusammenarbeit (GTZ) GmbH,
    // url=http://www.gtz.de/}, {providerid=bu_dwd, name=Deutscher Wetterdienst,
    // url=http://www.dwd.de/}, {providerid=bu_dlr, name=Deutsches Zentrum für
    // Luft- und Raumfahrt DLR e.V., url=http://www.dlr.de/},
    // {providerid=bu_kug, name=Koordinierungsstelle PortalU,
    // url=http://www.kst.portalu.de/}, {providerid=bu_labo,
    // name=Länderarbeitsgemeinschaft Boden LABO,
    // url=http://www.labo-deutschland.de/}, {providerid=bu_lawa,
    // name=Länderarbeitsgemeinschaft Wasser, url=http://www.lawa.de/},
    // {providerid=bu_laofdh, name=Leitstelle des Bundes für Abwassertechnik,
    // Boden- und Grundwasserschutz, Kampfmittelräumung und das
    // Liegenschaftsinformationssystem Außenanlagen LISA,
    // url=http://www.ofd-hannover.de/la/}, {providerid=bu_bpa, name=Presse- und
    // Informationsamt der Bundesregierung, url=http://www.bundesregierung.de/},
    // {providerid=bu_blauerengel, name=RAL/Umweltbundesamt Umweltzeichen
    // "Blauer Engel", url=http://www.blauer-engel.de/}, {providerid=bu_sru,
    // name=Rat von Sachverständigen für Umweltfragen (SRU),
    // url=http://www.umweltrat.de/}, {providerid=bu_ssk,
    // name=Strahlenschutzkommission, url=http://www.ssk.de/},
    // {providerid=bu_umk, name=Umweltministerkonferenz,
    // url=http://www.umweltministerkonferenz.de/}, {providerid=bu_wbgu,
    // name=Wissenschaftlicher Beirat der Bundesregierung Globale
    // Umweltveränderungen - WBGU, url=http://www.wbgu.de/},
    // {providerid=bu_agenda, name=Agenda-Transfer. Agentur für Nachhaltigkeit
    // GmbH, url=http://www.agenda-transfer.de/}, {providerid=bu_uga,
    // name=Umweltgutachterausschuss (UGA), url=http://www.uga.de/},
    // {providerid=bu_co2, name=co2online gGmbH Klimaschutzkampagne,
    // url=http://www.co2online.de/}, {providerid=bu_dekade, name=Weltdekade
    // ?Bildung für nachhaltige Entwicklung?,
    // url=http://www.dekade.org/index.htm}]
    // key : name
    // value: (java.lang.String) Bund
    List<InternalPartner> ret = new ArrayList<InternalPartner>();
    InternalPartner actualPartner = new InternalPartner();

    for (Map<String, Serializable> partner : allPartnerWithProvider) {
      for (Entry<String, Serializable> obj : partner.entrySet()) {
        String key = obj.getKey();
        Serializable value = obj.getValue();

        if (key.equals("partnerid")) {
          if (actualPartner.getId() != null) {
            ret.add(actualPartner);
            LOG.debug("parsed: '" + actualPartner + "'.");
            actualPartner = new InternalPartner();
          }
          actualPartner.setId((String) value);
        } else if (key.equals("providers")) {
          if (value instanceof List<?>) {
            // contains information about all providers to this partner
            for (Object providerInfos : ((List<?>) value)) {
              // summarize information for ONE provider
              if (providerInfos instanceof Map<?, ?>) {
                Set<?> providerInfosEntries = ((Map<?, ?>) providerInfos).entrySet();
                InternalProvider actualProvider = new InternalProvider();
                for (Map.Entry<?, ?> providerInfo : (Set<Map.Entry<?, ?>>) providerInfosEntries) {
                  String keyProvider = (String) providerInfo.getKey();
                  Object valueProvider = providerInfo.getValue();
                  if (keyProvider.equals("name")) {
                    actualProvider.setName((String) valueProvider);
                  } else if (keyProvider.equals("providerid")) {
                    actualProvider.setId((String) valueProvider);
                  } else if (keyProvider.equals("url")) {
                    if (valueProvider instanceof String) {
                      actualProvider.addUrl((String) valueProvider);
                    } else {
                      LOG.warn("for provider with key 'url' got unexpeced object of type: " + valueProvider.getClass());
                    }
                  }
                }
                actualPartner.addProviders(actualProvider);
              }
            }
          }
        } else if (key.equals("name")) {
          actualPartner.setName((String) value);
        }
      }
    }
    if (actualPartner.getName() != null) {
      ret.add(actualPartner);
    }
    return ret;
  }
}
