<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or â€“ as soon they will be
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 

<%@page import="de.ingrid.admin.security.IngridPrincipal"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.easytabs.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="../js/mindmup-editabletable.js"></script>

<script type="text/javascript">
	$(document).ready(
		function() {
			$('#tab-container').easytabs({
				animate : false
			});

			$('#schedulingTabs').easytabs({
				animate : false
			});

			$("#configurationTable").tablesorter({
				headers : {
					2 : {
						sorter : false
					},
					3 : {
						sorter : false
					},
					4 : {
						sorter : false
					}
				}
			});

			$('#configurationTable').editableTableWidget({
				editor : $('<textarea>')
			});

			$('#configurationTable td').on(
				'change',
				function(evt, newValue) {
					var row = evt.target.parentNode;
					var columnKey = $("td", row)[1];
					var key = $(columnKey).text();
					//YAHOO.util.Connect.asyncRequest('POST', 'index.html?name=' + record.getData('name')+'&value='+oNewData);
					$.post("instanceConfig.html?instance=${instance.name}&name=" + key + "&value=" + newValue,
						function(data) {
							console.log("OK: ", data);
						}
					);
				}
			);
		}
	);

	function selectTab(id) {
		$('#tabsInstance').show();
	}
</script>

</head>
<body>
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
		<h1 id="head">
			<a href="listInstances.html">&lt;- Zurück</a> Instanz: ${instance.name}
		</h1>
		<div id="tab-container" class="tab-container">
			<ul class='etabs'>
				<li class='tab'><a href="#tab-config">Konfiguration</a></li>
				<li class='tab'><a href="#tab-urls">URL-Pflege</a></li>
				<!-- <li class='tab'><a href="#tab-schedule">Scheduling</a></li> -->
				<li class='tab'><a href="dbParams.html" data-target="#tab-schedule">Scheduling - ajax</a></li>
				<li class='tab'><a href="#tab-status">Status</a></li>
			</ul>
			<div class="panel-container">
				<div id="tab-config">
					<c:import url="includes/config.jsp"></c:import>
				</div>


				<div id="tab-urls">
					<h2>Content for urls</h2>
					<table id="urlTable" class="data tablesorter">
						<thead>
							<tr>
								<th data-sort="string">URL</th>
								<th data-sort="string">Status</th>
								<th data-sort="string"></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${instance.urls}" var="url" varStatus="loop">
								<tr>
									<td>${url.url}</a></td>
									<td>${url.status}</td>
									<%-- <td><button type="button" action="delete" name="delete" data-id="${instance.name}">Löschen</button></td> --%>
									<td><a href="instance.html?id=${url.id}&editUrl">Bearbeiten</a> <a
										href="instance.html?id=${url.id}&deleteUrl">Löschen</a> <a href="instance.html?id=${url.id}&testUrl">Test</a>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
					<button type="submit" name="add">Neue URL</button>
				</div>


				<div id="tab-schedule">
					<!-- <c:import url="includes/scheduling.jsp"></c:import> -->
				</div>
				<div id="tab-status">
					<h2>Content for status</h2>
				</div>
			</div>
		</div>
	</div>

	<div id="footer" style="height: 100px; width: 90%"></div>
</body>
</html>

