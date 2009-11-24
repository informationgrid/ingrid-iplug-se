package de.ingrid.iplug.se.urlmaintenance;

import java.io.Serializable;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
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
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.security.PortaluPrincipal;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.propertyEditorSupport.PartnerPropertyEditorSupport;
import de.ingrid.iplug.se.urlmaintenance.service.PartnerAndProviderDbSyncService;
import edu.emory.mathcs.backport.java.util.Collections;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/index.html")
public class UrlMaintenanceController extends NavigationSelector {

  // private final Log LOG = LogFactory.getLog(UrlMaintenanceController.class);

  private final IPartnerDao _partnerDao;
  private final PartnerAndProviderDbSyncService _syncService;

  @Autowired
  public UrlMaintenanceController(IPartnerDao partnerDao, PartnerAndProviderDbSyncService syncService) {
    _partnerDao = partnerDao;
    _syncService = syncService;
  }

//  @ModelAttribute("partners")
//  public List<Partner> referenceDataPartners() {
//    return _partnerDao.getAll();
//  }
//
//  @ModelAttribute("providers")
//  public List<Provider> referenceDataProviders() {
//    return _providerDao.getAll();
//  }

  @RequestMapping(method = RequestMethod.GET)
  public String urlMaintenance(HttpServletRequest httpRequest, Model model) {
    Principal userPrincipal = httpRequest.getUserPrincipal();
    if (userPrincipal != null) {
      List<Map<String, Serializable>> allPartnerWithProvider = ((PortaluPrincipal) userPrincipal).getAllPartnerWithProvider();
      if (allPartnerWithProvider != null && allPartnerWithProvider.size() > 0) {
        _syncService.syncDb(allPartnerWithProvider);
      }
    }
    model.addAttribute("partnerProviderCommand", new PartnerProviderCommand());
    List<Partner> partners = _partnerDao.getAll();
    model.addAttribute("partners", partners);
    
    model.addAttribute("jsonMap", createPartnerAndProviderJsonMap());
    return "urlmaintenance";
  }

  private String createPartnerAndProviderJsonMap() {
    StringWriter stringWriter = new StringWriter();
    try {
        JsonGenerator jsonGenerator = new ObjectMapper().getJsonFactory().createJsonGenerator(stringWriter);
        jsonGenerator.writeStartObject();
        for(Partner partner : _partnerDao.getAll()){
          jsonGenerator.writeArrayFieldStart(String.valueOf(partner.getId()));
          List<Provider> providers = new ArrayList<Provider>(partner.getProviders());
          Collections.sort(providers, new Comparator<Provider>(){

            @Override
            public int compare(Provider o1, Provider o2) {
              return o1.getName().compareToIgnoreCase(o2.getName());
            }});
          for(Provider provider : providers){
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", provider.getId());
            jsonGenerator.writeStringField("name", StringEscapeUtils.escapeHtml(provider.getName()));
            jsonGenerator.writeEndObject();
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
  public String postUrlMaintenance(@ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    return "redirect:/welcomeEditUrls.html";
  }

  @InitBinder
  public void initBinder(ServletRequestDataBinder binder) {
      binder.registerCustomEditor(Partner.class, new PartnerPropertyEditorSupport(_partnerDao));
  }
}
