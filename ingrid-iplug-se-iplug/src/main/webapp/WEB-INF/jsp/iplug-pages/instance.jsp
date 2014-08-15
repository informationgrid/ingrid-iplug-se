<%@ include file="/WEB-INF/jsp/base/include.jsp"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

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
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css"
	media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css"
	media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.easytabs.min.js"></script>

<script type="text/javascript">
	$(document).ready(function() {
		$('#tab-container').easytabs({
			animate : false
		});
	});
	
	function selectTab(id) {
		$('#tabsInstance' ).show();
	}
</script>

</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60"
			alt="Portal U" />
		<h1>
			<fmt:message key="DatabaseConfig.main.configuration" />
		</h1>
		<%
		    java.security.Principal principal = request.getUserPrincipal();
		    if (principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
		%>
		<div id="language">
			<a href="../base/auth/logout.html"><fmt:message
					key="DatabaseConfig.main.logout" /></a>
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
		<h1 id="head"><a href="listInstances.html">&lt;- Zurück</a> Instanz: ${instance.name}</h1>
		<div id="tab-container" class="tab-container">
			<ul class='etabs'>
				<li class='tab'><a href="#tab-config">Konfiguration</a></li>
				<li class='tab'><a href="#tab-urls">URL-Pflege</a></li>
				<li class='tab'><a href="#tab-schedule">Scheduling</a></li>
				<li class='tab'><a href="#tab-status">Status</a></li>
			</ul>
			<div class="panel-container">
				<div id="tab-config">
					<h2>Content for config</h2>
				</div>
				<div id="tab-urls">
					<h2>Content for urls</h2>
				</div>
				<div id="tab-schedule">
					<h2>Content for schedule</h2>
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

