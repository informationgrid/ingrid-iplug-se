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
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />
	
<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("button[action]").click(function() {
            // get form
            var form = $("#formInstances");
            var action = $(this).attr("action");
            // set request action
            if(action) {
                form.find("input[name='action']").val(action);
            }
            // get button id
            var id = $(this).attr("data-id");
            // set request id
            if(id) {
                form.find("input[name='id']").val(id);
            }
            // submit form
            form.submit();
        });
        
        $("#listInstances").tablesorter({ 
            // pass the headers argument and assing a object 
            headers: { 
                // assign the secound column (we start counting zero) 
                1: { 
                    // disable it by setting the property sorter to false 
                    sorter: false 
                }, 
                // assign the third column (we start counting zero) 
                2: { 
                    // disable it by setting the property sorter to false 
                    sorter: false 
                } 
            }
        });
    });
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
		<h1 id="head">SE - Instanzen</h1>
        
        <div class="controls">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('formInstances').submit();">Weiter</a>
        </div>
        <div class="controls cBottom">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('formInstances').submit();">Weiter</a>
        </div>

        <form:form id="formInstances" method="post" action="../iplug-pages/listInstances.html">
            <input type="hidden" name="action" value="submit" />
            <input type="hidden" name="id" value="" />
			<div id="instances">
				<table id="listInstances" class="data tablesorter">
					<thead>
						<tr>
							<th data-sort="string">Name</th>
							<th data-sort="string">Status</th>
							<th data-sort="string">Aktionen</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${instances}" var="instance" varStatus="loop">
							<tr>
								<td><a href="instanceConfig.html?instance=${instance.name}">${instance.name}</a></td>
								<td>${instance.status}</td>
								<%-- <td><button type="button" action="delete" name="delete" data-id="${instance.name}">Löschen</button></td> --%>
								<td><a href="listInstances.html?instance=${instance.name}&delete">Löschen</a></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				<input type="text" name="instance" style="width: 200px;"></input>
		        <button type="submit" name="add">Neue Instanz</button>
                <c:if test="${not empty error}">
                    <p class="error">${error}</p>
                </c:if>
			</div>
		</form:form>

	</div>

	<div id="footer" style="height: 100px; width: 90%"></div>
</body>
</html>

