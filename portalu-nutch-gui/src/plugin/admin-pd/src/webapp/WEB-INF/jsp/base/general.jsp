<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal"%><html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>Portal U Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
		<h1>Konfiguration</h1>
		<%
		java.security.Principal  principal = request.getUserPrincipal();
		if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
		%>
			<div id="language"><a href="<%=request.getContextPath()%>/base/auth/logout.html">Logout</a></div>
		<%
		}
		%>
	</div>
	
	<div id="help"><a href="#">[?]</a></div>
	
	<c:set var="active" value="general" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Angaben zu Betreiber und Datenquelle</h1>
		<div class="controls">
			<a href="#" onclick="document.location='workingDir.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='workingDir.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.getElementById('plugDescription').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Allgemeine Angaben zum Betreiber</h2>
			<form:form method="post" action="general.html" modelAttribute="plugDescription"> 
			    <c:if test="${isIgc}"><form:hidden path="proxyServiceURL" /></c:if>
				<table id="konfigForm">
					<tr>
						<td colspan="2"><h3>Ansprechpartner:</h3></td>
					</tr>
					<tr>
						<td>Titel:</td>
						<td>
							<form:input path="personTitle" /><br />
                            <span>Der Titel des Ansprechpartners (optional).</span>
						</td>
					</tr> 
					<tr>  
						<td>Nachname:</td>
						<td><form:input path="personSureName" /><br /><span>Der Name des Ansprechpartners.</span><form:errors path="personSureName" cssClass="error" element="div" /></td>
					</tr> 
					<tr>  
						<td>Vorname:</td>
						<td><form:input path="personName" /><br /><span>Der Vorname des Ansprechpartners.</span><form:errors path="personName" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td>Telefon:</td>
						<td><form:input path="personPhone" /><br /><span>Die Telefonnummer unter der der Ansprechpartner erreichbar ist.</span><form:errors path="personPhone" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td>E-Mail:</td>
						<td><form:input path="personMail" /><br /><span>Die E-Mail Adresse des Ansprechpartners.</span><form:errors path="personMail" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td colspan="2"><h3>Datenquelle:</h3></td>
					</tr>					
					<tr>
						<td>Name der Datenquelle:</td>
						<td><form:input path="dataSourceName" /><br /><span>Der Name der die Datenquelle bezeichnet.</span><form:errors path="dataSourceName" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td>Kurzbeschreibung:</td>
						<td><form:textarea path="dataSourceDescription" /><br /><span>Eine kurze Beschreibung des Inhalts und der Anforderung der Datenquelle (optional).</span></td>
					</tr>
					<tr>
						<td colspan="2"><h3>Administrationsinterface:</h3></td>
					</tr>					
					<tr>
						<td>URL:</td>
						<td><form:input path="iplugAdminGuiUrl" /><br /><span>Die Adresse unter der dieses Administrationsinterface erreichbar sein soll.</span><form:errors path="iplugAdminGuiUrl" cssClass="error" element="div" /></td>
					</tr>
					<tr>
						<td>Port:</td>
						<td><form:input path="iplugAdminGuiPort" /><br /><span>Der Port unter der dieses Administrationsinterface erreichbar sein soll.</span><form:errors path="iplugAdminGuiPort" cssClass="error" element="div" /></td>
					</tr>
				</table>
			</form:form>
		</div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>