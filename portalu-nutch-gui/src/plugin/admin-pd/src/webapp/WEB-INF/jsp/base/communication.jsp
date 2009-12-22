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
<script type="text/javascript" src="../js/base/jquery-1.3.2.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("button[action], .submit").click(function() {
        	// get form
            var form = $("#communication");
            // get button action
            var action = $(this).attr("action");
            // set request action
            if(action) {
                form.find("input[name='action']").val(action);
            }
            // get button id
            var id = $(this).attr("id");
            // set request id
            if(id) {
                form.find("input[name='id']").val(id);
            }
            // submit form
            form.submit();
        });
    });
</script>
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
	
	<c:set var="active" value="communication" scope="request"/>
	<c:import url="subNavi.jsp"></c:import>
	
	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Hinzufügen des iBus</h1>
		<div class="controls">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='welcome.html';" class="submit">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="#" onclick="document.location='welcome.html';">Zurück</a>
			<a href="#" onclick="document.location='welcome.html';">Abbrechen</a>
			<a href="#" onclick="document.location='welcome.html';" class="submit">Weiter</a>
		</div>
		<div id="content">
		
            <form:form method="post" action="communication.html" modelAttribute="communication">
                <input type="hidden" name="action" value="submit" />
                <input type="hidden" name="id" value="" />
                <table id="konfigForm">
                    <tr>
                        <td colspan="2"><h3>Ihr IPlug:</h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Eigene Proxy Service Url:</td>
                        <td>
                            <input type="text" name="proxyServiceUrl" value="${communication.proxyServiceUrl}" /><br />
                            <span>Der Name mit den man diesen IPlug identifiziert.</span>
                            <form:errors path="proxyServiceUrl" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><h3>IBus angaben:</h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">IBus Proxy Service Url:</td>
                        <td>
                            <input type="text" name="busProxyServiceUrl" value="${communication.busProxyServiceUrl}" /><br />
                            <span>Der Name des IBus mit dem sich der IPlug verbinden soll.</span>
                            <form:errors path="busProxyServiceUrl" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">IP:</td>
                        <td>
                            <input type="text" name="ip" value="${communication.ip}" /><br />
                            <span>Die IP-Adresse des IBus.</span>
                            <form:errors path="ip" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">Port:</td>
                        <td>
                            <input type="text" name="port" value="${communication.port}" /><br />
                            <span>Der Port des IBus.</span>
                            <form:errors path="port" cssClass="error" element="div" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">&nbsp;</td>
                        <td><button type="button" action="add"><c:choose><c:when test="${!empty busses}">Hinzufügen</c:when><c:otherwise>Speichern</c:otherwise></c:choose></button></td>
                    </tr>
                </table>
                
	            <c:if test="${!empty busses}">
	                   <h3>Vorhandene IBusse</h3>
		              <table class="data">
		                  <tr>
		                      <th>IBus Url</th>
		                      <th>IP</th>
		                      <th>Port</th>
		                      <th>&nbsp;</th>
		                      <th>&nbsp;</th>
		                  </tr>
		                  <c:set var="busIndex" value="0" />
		                  <c:forEach items="${busses}" var="bus">
		                      <tr>
		                          <td>${bus.busProxyServiceUrl}</td>
		                          <td>${bus.ip}</td>
		                          <td>${bus.port}</td>
		                          <td>
		                              <c:choose>
		                                  <c:when test="${busIndex == 0}">(Standard)</c:when>
		                                  <c:otherwise><button type="button" action="set" id="${busIndex}"/>Als Standard</button></c:otherwise>
		                              </c:choose>
		                          </td>
		                          <td><button type="button" action="delete" id="${busIndex}"/>Löschen</button></td>
		                      </tr>
		                      <c:set var="busIndex" value="${busIndex + 1}" />
		                  </c:forEach>
		              </table>
	            </c:if>
            </form:form>
		
	    </div>
	</div>
	<div id="footer" style="height:100px; width:90%"></div>
</body>
</html>