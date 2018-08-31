<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
<%@ include file="/WEB-INF/jsp/base/include.jsp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="de.ingrid.admin.security.IngridPrincipal"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> - Instanzen</title>
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

        var dialogConfirm = $( "#dialog-confirm" ).dialog({
            resizable: false,
            autoOpen: false,
            height: 140,
            modal: true,
            buttons: {
                "Abbrechen": function() {
                    $( this ).dialog( "close" );
                },
                "Löschen": function() {
                    var action = dialogConfirm.data("action");
                    $.ajax({
                        type: 'DELETE',
                        url: "../rest/instance/" + action.id,
                        success: function() {
                            window.location = window.location.href
                        },
                        contentType: 'text/plain'
                    });
                    $( this ).dialog( "close" );
                }
            }
        });

        var dialogInstance = $( "#dialog-instance" ).dialog({
            resizable: false,
            autoOpen: false,
            height: 180,
            modal: true,
            buttons: {
                "Abbrechen": function() {
                    $( this ).dialog( "close" );
                },
                "Anlegen": function() {
                    var instance = $("#instanceName").val();
                    var from = dialogInstance.data("from");
                    $.ajax({
                        type: 'POST',
                        url: "listInstances.html?instance=" + instance + "&duplicateFrom=" + from + "&add",
                        success: function() {
                            location.reload();
                        },
                        contentType: 'application/json'
                    });
                    $( this ).dialog( "close" );
                }
            },
            open: function() {
                // clear input
                $("#instanceName").val("");
            }
        });

        $(".btnInstance").button().click(function() {
            var id = $(this).parents("tr").attr("data-id");
            location.href = "instanceManagement.html?instance=" + id;

        }).next().button({
            text: false,
            icons: {
                primary: "ui-icon-triangle-1-s"
            }
        }).click(function() {
            var menu = $(this).parent().next().show().position({
                my: "left top",
                at: "left bottom",
                of: this
            });
            $(document).one("click", function(evt) {
                var action = evt.target.getAttribute("action");
                var id = $(evt.target).parents("tr").attr("data-id");
                if (action == "delete") {
                    dialogConfirm.data("action", {
                        id: id
                    });
                    dialogConfirm.dialog( "open" );

                } else if (action == "duplicate") {
                    dialogInstance.data( "from", id );
                    dialogInstance.dialog( "open" );
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

<% if (request.isUserInRole( "admin" )) { %>
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
<% } %>

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
                                <td><a href="instanceManagement.html?instance=${instance.name}">${instance.name}</a></td>
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
<% if (request.isUserInRole( "admin" )) { %>
                                            <button class="select">Weitere Optionen</button>
<% } %>                
                                        </div>
<% if (request.isUserInRole( "admin" )) { %>
                                        <ul style="position:absolute; padding-left: 0; min-width: 140px; z-index: 100;">
                                            <li action="duplicate">Kopie erzeugen</li>
                                            <li action="delete">Löschen</li>
                                        </ul>
<% } %>                
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
<% if (request.isUserInRole( "admin" )) { %>
                <div class="input inline">
                    <input type="text" name="instance" style="width: 200px;"></input>
                </div>
                <button type="submit" name="add">Neue Instanz</button>
                <c:if test="${not empty error}">
                    <p class="error">${error}</p>
                </c:if>
<% } %>                
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

    <div id="dialog-instance" title="Instanz duplizieren?">
        <div>
            <h3>Name der Instanz</h3>
            <div class="input full">
                <input type="text" id="instanceName" value="" class="text ui-widget-content ui-corner-all">
            </div>
        </div>
    </div>

</body>
</html>

