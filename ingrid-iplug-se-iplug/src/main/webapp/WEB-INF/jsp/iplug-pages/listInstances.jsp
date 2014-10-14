<%@ include file="/WEB-INF/jsp/base/include.jsp"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@page import="de.ingrid.admin.security.IngridPrincipal"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> - Instanzen</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />
	
<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        /* $("button[action]").click(function() {
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
        }); */
        
        $("#listInstances").tablesorter();
        
        // initialize radio buttons for status of instances
        $( ".radio" ).buttonset();
        
        $( ".radio" ).on("change", function(event) {
        	var instance = $(this).attr( "data-id" );
        	var value = $(event.target).val();
        	
        	$.ajax({
                type: "POST",
                url: "../rest/instance/" + instance + "/" + value,
                contentType: 'application/json',
                success: function() {
                    console.debug( "Switched instance '" + instance + "' to: " + value );
                },
                error: function(jqXHR, text, error) {
                    console.error(text, error);
                }
            });
        });

        dialogConfirm = $( "#dialog-confirm" ).dialog({
            resizable: false,
            autoOpen: false,
            height:140,
            modal: true,
            buttons: {
                "Abbrechen": function() {
                    $( this ).dialog( "close" );
                },
                "Löschen": function() {
                    var action = dialogConfirm.data("action");
                    $.ajax({
                        url: "listInstances",
                        type: 'DELETE',
                        success: function() {
                            location.reload();
                        },
                        data: action.id,
                        contentType: 'application/json'
                    });
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $(".btnInstance").button().click(function() {
            var id = $(this).parents("tr").attr("data-id");
            location.href = "instanceUrls.html?instance=" + id;

        }).next().button({
            text : false,
            icons : {
                primary : "ui-icon-triangle-1-s"
            }
        }).click(function() {
            var menu = $(this).parent().next().show().position({
                my : "left top",
                at : "left bottom",
                of : this
            });
            $(document).one("click", function(evt) {
                var action = evt.target.getAttribute("action");
                var id = $(evt.target).parents("tr").attr("data-id");
                if (action == "recreateIndex") {
                    $.post("listInstances.html?instance=" + id + "&" + action, null, function() {
                    	location.reload();
                    });
                } else if (action == "delete") {
                    dialogConfirm.data("action", {
                        id: id
                    });
                    dialogConfirm.dialog( "open" );
                }
                menu.hide();
            });
            return false;
        }).parent().buttonset().next().hide().menu();
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
							<th data-sorter="false" class="sorter-false" width="100px">Status</th>
							<th data-sorter="false" width="150px">Aktionen</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${instances}" var="instance" varStatus="loop">
							<tr data-id="${instance.name}">
								<td><a href="instanceUrls.html?instance=${instance.name}">${instance.name}</a><c:if test="${ instance.indexTypeExists == false }"> <span class="error">(Index/Typ fehlt)</span></c:if></td>
								<td>${instance.status}
                                    <div class="radio" data-id="${instance.name}">
                                        <input type="radio" id="statusOn_${instance.name}" name="status_${instance.name}" value="on"
                                            <c:if test="${instance.isActive == true}">checked="checked"</c:if>
                                        ><label for="statusOn_${instance.name}">An</label>
                                        <input type="radio" id="statusOff_${instance.name}" name="status_${instance.name}" value="off" 
                                            <c:if test="${instance.isActive == false}">checked="checked"</c:if>
                                        ><label for="statusOff_${instance.name}">Aus</label>
                                    </div>
                                </td>
								<%-- <td><button type="button" action="delete" name="delete" data-id="${instance.name}">Löschen</button></td> --%>
								<td>
                                    <%-- <a href="listInstances.html?instance=${instance.name}&delete">Löschen</a> --%>
                                    <div>
                                        <div>
                                            <button type="button" class="btnInstance" data-id="${ instance.name }">Bearbeiten</button>
                                            <button class="select">Weitere Optionen</button>
                                        </div>
                                        <ul style="position:absolute; padding-left: 0; min-width: 100px; z-index: 100;">
                                            <li action="delete">Löschen</li>
                                            <li action="recreateIndex" <c:if test="${ instance.indexTypeExists == true }">disabled</c:if>>Index/Typ erstellen</li>
                                        </ul>
                                    </div>
                                </td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
                <div class="input inline">
				    <input type="text" name="instance" style="width: 200px;"></input>
                </div>
		        <button type="submit" name="add">Neue Instanz</button>
                <c:if test="${not empty error}">
                    <p class="error">${error}</p>
                </c:if>
			</div>
		</form:form>

	</div>

	<div id="footer" style="height: 100px; width: 90%"></div>

    <div id="dialog-confirm" title="Wirklich löschen?">
        <p>
            <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
            Möchten Sie die Instanz wirklich löschen?
        </p>
    </div>

</body>
</html>

