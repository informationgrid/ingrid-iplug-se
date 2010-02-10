<%@page import="de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand"%>
<%@page import="de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner"%>
<%@page import="de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider"%>
<div style="text-align:right;float:right;margin:10px">
<%
java.security.Principal  principal = request.getUserPrincipal();
PartnerProviderCommand partnerProvider = (PartnerProviderCommand) request.getSession().getAttribute("partnerProviderCommand");
if(principal != null) {
%>
	<a href ="<%=request.getContextPath()%>/auth/logout.html" style="color:black">Logout</a>
<%
}
if(partnerProvider != null) {
   Partner partner = partnerProvider.getPartner();
   Provider provider = partnerProvider.getProvider();
   if (partner != null && provider != null) {
%>
	<br/>
	<span style="color: #335; font-size: 11px;"><%= partner.getName() %><br/><%= provider.getName() %></span>
<%
   }
}
%>
</div>
<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/logo.gif" />