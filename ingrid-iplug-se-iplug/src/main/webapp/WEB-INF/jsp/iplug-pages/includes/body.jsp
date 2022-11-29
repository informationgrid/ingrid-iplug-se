<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2022 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  http://ec.europa.eu/idabc/eupl5
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<div id="header">
    <img src="../images/base/logo.gif" width="168" height="60" alt="InGrid" />
    <h1>
        <fmt:message key="DatabaseConfig.main.configuration" />
    </h1>
    <security:authorize access="isAuthenticated()">
    <div id="language">
        <a href="../base/auth/logout.html"><fmt:message key="DatabaseConfig.main.logout" /></a>
    </div>
    </security:authorize>>
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
<% if (request.isUserInRole( "admin" )) { %>
            <li class='tab<c:if test="${activeTab == '1'}"> active</c:if>'><a<c:if test="${activeTab != '1'}"> href="instanceConfig.html?instance=${instance.name}"</c:if>>Konfiguration</a></li>
<% } %>
            <li class='tab<c:if test="${activeTab == '3'}"> active</c:if>'><a<c:if test="${activeTab != '3'}"> href="instanceScheduling.html?instance=${instance.name}"</c:if>>Zeitplanung</a></li>
            <li class='tab<c:if test="${activeTab == '4'}"> active</c:if>'><a<c:if test="${activeTab != '4'}"> href="instanceManagement.html?instance=${instance.name}"</c:if>>Management</a></li>
            <li class='tab<c:if test="${activeTab == '5'}"> active</c:if>'><a<c:if test="${activeTab != '5'}"> href="instanceSearch.html?instance=${instance.name}"</c:if>>Suche</a></li>
            <li class='tab<c:if test="${activeTab == '6'}"> active</c:if>'><a<c:if test="${activeTab != '6'}"> href="instanceReports.html?instance=${instance.name}"</c:if>>Reports</a></li>
<% if (request.isUserInRole( "admin" )) { %>
            <li class='tab<c:if test="${activeTab == '7'}"> active</c:if>'><a<c:if test="${activeTab != '7'}"> href="instanceAdmins.html?instance=${instance.name}"</c:if>>Administratoren</a></li>
<% } %>
        </ul>
        <div class="panel-container">
            <div>
                <c:if test="${activeTab == '2'}"><c:import url="includes/urls.jsp"></c:import></c:if>
                <c:if test="${activeTab == '1'}"><c:import url="includes/config.jsp"></c:import></c:if>
                <c:if test="${activeTab == '3'}"><c:import url="includes/scheduling.jsp"></c:import></c:if>
                <c:if test="${activeTab == '4'}"><c:import url="includes/management.jsp"></c:import></c:if>
                <c:if test="${activeTab == '5'}"><c:import url="includes/search.jsp"></c:import></c:if>
                <c:if test="${activeTab == '6'}"><c:import url="includes/reports.jsp"></c:import></c:if>
                <c:if test="${activeTab == '7'}"><c:import url="includes/admins.jsp"></c:import></c:if>
            </div>
        </div>
    </div>
</div>

<div id="footer" style="height: 100px; width: 90%"></div>
