<%@ include file="/WEB-INF/jsp/base/include.jsp"%>
<%@page import="de.ingrid.admin.security.IngridPrincipal"%>

<div id="header">
    <img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
    <h1>
        <fmt:message key="DatabaseConfig.main.configuration" />
    </h1>
    <%
        java.security.Principal principal = request.getUserPrincipal();
        if (principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
    %>
    <div id="language">
        <a href="../base/auth/logout.html"><fmt:message key="DatabaseConfig.main.logout" /></a>
    </div>
    <%
        }
    %>
</div>
<div id="help">
    <a href="#">[?]</a>
</div>

<c:set var="active" value="listInstances" scope="request" />
<c:import url="../base/subNavi.jsp"></c:import>
<div id="contentBox" class="contentMiddle">
    <h1 id="head">Instanz: ${instance.name}</h1>
    <div class="controls">
        <a href="listInstances.html">Zur&uuml;ck zur &Uuml;bersicht</a>
        <!-- <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('seSettings').submit();">Weiter</a> -->
    </div>
    <div id="tab-container" class="tab-container">
        <ul class='etabs'>
            <li class='tab<c:if test="${activeTab == '2'}"> active</c:if>'><a<c:if test="${activeTab != '2'}"> href="instanceUrls.html?instance=${instance.name}"</c:if>>URL-Pflege</a></li>
            <li class='tab<c:if test="${activeTab == '1'}"> active</c:if>'><a<c:if test="${activeTab != '1'}"> href="instanceConfig.html?instance=${instance.name}"</c:if>>Konfiguration</a></li>
            <li class='tab<c:if test="${activeTab == '3'}"> active</c:if>'><a<c:if test="${activeTab != '3'}"> href="instanceScheduling.html?instance=${instance.name}"</c:if>>Zeitplanung</a></li>
            <li class='tab<c:if test="${activeTab == '4'}"> active</c:if>'><a<c:if test="${activeTab != '4'}"> href="instanceManagement.html?instance=${instance.name}"</c:if>>Management</a></li>
        </ul>
        <div class="panel-container">
            <div id="tab-config">
                <c:if test="${activeTab == '2'}"><c:import url="includes/urls.jsp"></c:import></c:if>
                <c:if test="${activeTab == '1'}"><c:import url="includes/config.jsp"></c:import></c:if>
                <c:if test="${activeTab == '3'}"><c:import url="includes/scheduling.jsp"></c:import></c:if>
                <c:if test="${activeTab == '4'}"><c:import url="includes/management.jsp"></c:import></c:if>
            </div>
        </div>
    </div>
</div>

<div id="footer" style="height: 100px; width: 90%"></div>