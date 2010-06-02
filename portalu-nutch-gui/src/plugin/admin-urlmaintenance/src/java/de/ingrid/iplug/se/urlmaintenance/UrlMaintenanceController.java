package de.ingrid.iplug.se.urlmaintenance;

import java.io.Serializable;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.NavigationSelector;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.security.PortaluPrincipal;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.service.PartnerAndProviderDbSyncService;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/index.html")
public class UrlMaintenanceController extends NavigationSelector {

  private final Log LOG = LogFactory.getLog(UrlMaintenanceController.class);

  private final IPartnerDao _partnerDao;
  private final IProviderDao _providerDao;
  private final PartnerAndProviderDbSyncService _syncService;

  @Autowired
  public UrlMaintenanceController(IPartnerDao partnerDao, IProviderDao providerDao,
      PartnerAndProviderDbSyncService syncService) {
    _partnerDao = partnerDao;
    _providerDao = providerDao;
    _syncService = syncService;
  }
  
  @ModelAttribute("contextPath")
  public String contextPath(final HttpServletRequest request) {
      return request.getContextPath();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String urlMaintenance(@RequestParam(value = "error", required = false) final String error, HttpServletRequest httpRequest, Model model) {
    List<Map<String, Serializable>> allPartnerWithProvider = null;
    Principal userPrincipal = httpRequest.getUserPrincipal();
    if (userPrincipal != null && userPrincipal instanceof PortaluPrincipal) {
      allPartnerWithProvider = ((PortaluPrincipal) userPrincipal)
          .getAllPartnerWithProvider();
      if (allPartnerWithProvider != null && allPartnerWithProvider.size() > 0) {
        try {
          _syncService.syncDb(allPartnerWithProvider);
        } catch (Throwable ex) {
          LOG.error("Can not sync DB with partner/provider from ibus.", ex);
        }
        
        
      }
    }
    model.addAttribute("partnerProviderCommand", new PartnerProviderCommand());
    List<Partner> partners = filterByRights(allPartnerWithProvider, _partnerDao.getAll());
    model.addAttribute("partners", partners);
    model.addAttribute("error", error);

    model.addAttribute("jsonMap", createPartnerAndProviderJsonMap(getProviderFromPartner(allPartnerWithProvider)));
    return "urlmaintenance";
  }

  /**
   * Only show the partner the user is supposed to see.
   * 
   * @param partnerWithProvider, are those partner the user is associated with
   * @param allPartner, are all available partner
   * @return
   */
  private List<Partner> filterByRights(List<Map<String, Serializable>> partnerWithProvider,
        List<Partner> allPartner) {
      List<Partner> filteredList = new ArrayList<Partner>();
      for (Partner partner : allPartner) {
          for (Map<String, Serializable> map : partnerWithProvider) {
              if (map.get("partnerid").equals(partner.getShortName()))
                  filteredList.add(partner);
          }
      }
      
      // sort list of partner
      Collections.sort(filteredList, new Comparator<Partner>() {
        @Override
        public int compare(Partner p1, Partner p2) {
            return p1.getName().compareTo(p2.getName());
        }
      });
      return filteredList;
  }
  
  private List<String> getProviderFromPartner(List<Map<String, Serializable>> partnerWithProvider) {
      List<String> provider = new ArrayList<String>();
      for (Map<String, Serializable> map : partnerWithProvider) {
          for (Map<String, Serializable> map2 : (List<Map>) map.get("providers")) {
              provider.add((String)map2.get("providerid"));
          }
      }
      return provider;
  }

  private String createPartnerAndProviderJsonMap(List<String> allowedProvider) {
    StringWriter stringWriter = new StringWriter();
    try {
      JsonGenerator jsonGenerator = new ObjectMapper().getJsonFactory().createJsonGenerator(stringWriter);
      jsonGenerator.writeStartObject();
      for (Partner partner : _partnerDao.getAll()) {
        jsonGenerator.writeArrayFieldStart(String.valueOf(partner.getId()));
        List<Provider> providers = new ArrayList<Provider>(partner.getProviders());
        Collections.sort(providers, new Comparator<Provider>() {

          @Override
          public int compare(Provider o1, Provider o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
          }
        });
        for (Provider provider : providers) {
          // only write provider if user is allowed to see it
          if (allowedProvider.contains(provider.getShortName())) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", provider.getId());
            jsonGenerator.writeStringField("name", StringEscapeUtils.escapeHtml(provider.getName()));
            jsonGenerator.writeEndObject();
          }
        }
        jsonGenerator.writeEndArray();
      }
      jsonGenerator.writeEndObject();
      jsonGenerator.flush();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return stringWriter.toString();
  }

  @RequestMapping(method = RequestMethod.POST)
  public String postUrlMaintenance(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    return "redirect:/welcomeEditUrls.html";
  }

  @InitBinder
  public void initBinder(ServletRequestDataBinder binder) {
    binder.registerCustomEditor(Partner.class, new EntityEditor(_partnerDao));
    binder.registerCustomEditor(Provider.class, new EntityEditor(_providerDao));
  }
}
