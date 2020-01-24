<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> - Instanz-Administratoren-Pflege</title>
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/chosen.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery.tablesorter.pager.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.widgets.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.pager.min.js"></script>
<script type="text/javascript" src="../js/chosen.jquery.min.js"></script>
<script type="text/javascript" src="../js/mindmup-editabletable.js"></script>

<script type="text/javascript">
    var urlMaintenance = null;

    $(document).ready(function() {

        // remember initial default values which will be set for each new URL!
        var defaultValues = [];
        $("#dialog-form option:selected").each(function(index, item) {
            var metaSplitted = item.getAttribute("value").split(":");
            var meta = { metaKey: metaSplitted[0], metaValue: metaSplitted[1] };
            defaultValues.push( meta );
        });

        var pagerOptions = {
            container: $(".pager"),
            output: '{startRow} bis {endRow} von {filteredRows} Administratoren',
            //size: 2,
            savePages : false,
            ajaxUrl : '../rest/admins/${instance.name}?page={page}',
            serverSideSorting: true,
            ajaxObject: {
                dataType: 'json'
            },
            ajaxProcessing: function(data){
                if (data) {// && data.hasOwnProperty('rows')) {
                    var r, d = data.data,
                    // total number of rows (required)
                    total = data.totalAdmins,
                    // array of header names (optional)
                    //headers = data.headers,
                    // all rows: array of arrays; each internal array has the table cell data for that row
                    rows = "",
                    // len should match pager set size (c.size)
                    len = d.length;
                    // this will depend on how the json is set up - see City0.json
                    // rows
                    for ( r=0; r < len; r++ ) {
                        // row = ""; // new row array
                        // cells
                        rows += "<tr data-id='" + d[r].id + "'>" +
                                "<td><input type='checkbox'></td>" +
                                "<td>" + d[r].login + "</td>" +
                                "<td>" + actionButtonTemplate + "</td>" +
                                "</tr>";

                        //rows.push(row); // add new row array to rows array

                    }
                    //setTimeout( function() { createActionButton( $("tr .btnUrl") ); }, 0);

                    // in version 2.10, you can optionally return $(rows) a set of table rows within a jQuery object
                    return [ total, $(rows) ];
                }
            },
            customAjaxUrl: function(table, url) {
                // manipulate the url string as you desire
                var sort = $('#adminTable').data().tablesorter.sortList[0];

                if (sort) url += "&sort=" + sort.join(',');
                url += "&pagesize=" + this.size;

                return encodeURI(url);
            }
        };

        function isValidLogin( login ) {
            return (/^[A-Za-z0-9_-]*$/i.test( login ));
        }
        
        function isValidPassword( password ) {
            return password.length > 3;
        }

        function addAdminValidator() {
            var valid = true;
            $(".error").hide();

            var data = dialog.data("adminDataObject");

            // add all values from the dialog to the data object wich will be sent to the server
            data.login = $("#login").val();
            // check if login is valid
            if ( !isValidLogin( data.login ) ) {
                $(".error.login.format").show();
                valid = false;
            }
            
            data.password = $("#password").val();
            // check if login is valid
            if ( !isValidPassword( data.password ) ) {
                $(".error.password").show();
                valid = false;
            }
            
            if ( valid ) {
            	  $("#waitScreen").show();
                $.ajax({
                    type: "GET",
                    url: "../rest/isduplicateadmin/${instance.name}/" + data.login + "/",
                    success: function(result) {
                    	// disable duplicate test for updates                    	
                      if (result === "false" || dialog.data("isNew") === false ) {
                         $.ajax({
                             type: "POST",
                             url: "../rest/admin/${instance.name}/",
                             contentType: 'application/json',
                             data: JSON.stringify( data ),
                             success: function(data) {
                                    // let JSP do the magic to refresh the page correctly
                                    location.reload();
                             },
                             error: function(jqXHR, text, error) {
                                 console.error(text, error);
                                 $("#waitScreen").hide();
                             }
                         });

                         dialog.dialog( "close" );
                      } else {
                    	  $(".error.login.duplicate").show();
                    	  $("#waitScreen").hide();
                      }
                    },
                    error: function(jqXHR, text, error) {
                        console.error(text, error);
                        $("#waitScreen").hide();
                    }
                });            	              	
            }
            return valid;
        }
        

        function getButtonTemplate(type) {
            var button = null;
            button = "<div><button type='button' onclick='adminMaintenance.deleteRow(\"" + type + "\", event)'>Löschen</button>";
            return button;
        }

        function resetFields() {
            if (dialog.data("isNew") === true) {
                $(".ui-dialog-title", dialog.parent()).text( "Neuen Administrator anlegen" );
                $("#dialog-form").next().find("button:last .ui-button-text").text( "Erstellen" );

            } else {
                $(".ui-dialog-title", dialog.parent()).text( "Administrator bearbeiten" );
                $("#dialog-form").next().find("button:last .ui-button-text").text( "Ändern" );
            }

            // reset all select boxes
            $("#dialog-form select").each(function(index, item) {
                $(item).val("");
                $(item).trigger("chosen:updated");
            });

            // empty tables
            $("#dialog-form tbody tr:not(.newRow)").remove();

            $(".error").hide();

        }

        dialog = $("#dialog-form").dialog({
            autoOpen : false,
            height : 300,
            width : 770,
            modal : true,
            buttons : {
                "Abbrechen" : function() {
                    dialog.dialog("close");
                },
                /* "Erstellen und weitere anlegen ..." : addUrlValidator, */
                "Erstellen" : addAdminValidator
            },
            open: function() {
                // reset all fields first
                resetFields();

                var data = dialog.data("adminDataObject");
                if (data) {
                    $("#login").val( data.login ? data.login : "" );
                    $("#password").val( data.password ? data.password : "" );
                }
            },
            close : function() {}
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
                    if (action.type === "deleteSingleAdmin") {
                        $.ajax({
                            url: "../rest/admin/" + action.id,
                            type: 'DELETE',
                            success: function() {
                                location.reload();
                            },
                            data: action.id,
                            contentType: 'application/json'
                        });
                    } else if (action.type === "deleteMultipleAdmins") {
                        var checkedRows = $( "#adminTable input:checked" );
                        var dataIDs = [];
                        checkedRows.each( function(index, row) {
                            dataIDs.push( $( row ).parents("tr").attr("data-id") );
                        });
                        $.ajax({
                            type: "DELETE",
                            url: "../rest/admins",
                            contentType: 'application/json',
                            data: JSON.stringify( dataIDs ),
                            success: function() {
                                // let JSP do the magic to refresh the page correctly
                                location.reload();
                            },
                            error: function(jqXHR, text, error) {
                                console.error(text, error);
                            }
                        });
                    }
                    $( this ).dialog( "close" );
                }
            }
        });

        // hide all metadata types initially
        //$("#metadata span").hide();
        $( '#dialog-form input[type=radio]' ).change(function() {
            var parent = this.parentNode.parentNode;
            // hide all
            $("fieldset", parent).hide();
        });

        function deleteRow(type, evt) {
            actionHandler( type, $(evt.target) );
        }

        function actionHandler( action, target ) {
            var type = null,
                id = null;

            id = $( target ).parents("tr").attr("data-id");
            switch (action) {
            case "edit":
                $.get("../rest/admin/" + id, function(data) {
                    // reload page if data was not received, which calls login page
                    if (typeof data === "string") location.reload();
                    console.log("Data: ", data);
                    dialog.data("isNew", false);
                    dialog.data("adminDataObject", data);
                    dialog.dialog("open");
                });
                break;
            case "delete":
                dialogConfirm.data("action", {
                    type: "deleteSingleAdmin",
                    id: id
                });
                dialogConfirm.dialog("open");
                break;
            default:
                alert( "Unbekannte Aktion: ", action );
            }
        }

        function createActionButton( btn ) {
            btn.button().click(function() {
                var id = $( this ).parents("tr").attr("data-id");
                $.get("../rest/admin/" + id, function(data) {
                    // reload page if data was not received, which calls login page
                    if (typeof data === "string") location.reload();
                    console.log("Data: ", data);
                    dialog.data("isNew", false);
                    dialog.data("adminDataObject", data);
                    dialog.dialog("open");
                });
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
                    if (action) {
                        actionHandler( action, evt.target );
                    }
                    menu.hide();
                });
                return false;
            }).parent().buttonset().next().hide().menu();
        }

        $("#btnAddAdmin").on("click", function() {
            dialog.data("adminDataObject", {
                    instance: '${ instance.name }'
            });
            dialog.data("isNew", true);
            dialog.dialog("open");
        });

        // action for button to delete urls
        $("#btnDeleteAdmins").on( "click", function() {
            dialogConfirm.data( "action", {
                type: "deleteMultipleAdmins"
            });
            dialogConfirm.dialog( "open" );
        });

        var updateBrowserHistory = function() {
        	
            // avoid problem with missing functionality in IE9
            if (window.history.pushState)  {
                window.history.pushState(null, null, location.pathname + "?instance=${instance.name}");
            }
        };


        // initialize the table and its paging option
        $("#adminTable").tablesorter({
            headers : {
                0 : {
                    sorter : false
                },
                2 : {
                    sorter : false
                }
            },
            sortList: [[0,0]], // sort first column ascending
            delayInit: false,
            widgets: ['zebra', 'filter'],
            widgetOptions: {
                // filter_external: '#filterUrl',
                filter_serversideFiltering: true,
                filter_columnFilters: false,
                filter_hideFilters: false,
                // include child row content while filtering, if true
                // filter_childRows  : false,
                // class name applied to filter row and each input
                filter_cssFilter  : 'filtered',
                pager_removeRows: false
            }
        })
        .tablesorterPager(pagerOptions);

        $("#adminContent").css("visibility", "visible");
        $("#loading").hide();


        /* PUBLIC FUNCTIONS */
        adminMaintenance = {
            deleteRow: deleteRow,
            actionHandler: actionHandler
        };
    });

    var actionButtonTemplate =
        '<div class="actionButtons">' +
            // '<div>' +
            //     '<button class="btnUrl">Bearbeiten</button>' +
            //     '<button class="select">Weitere Optionen</button>' +
            // '</div>' +
            // '<ul style="position:absolute; padding-left: 0; min-width: 180px; z-index: 100; display: none;">' +
            //     '<li action="delete">Löschen</li>' +
            //     '<li action="test">Testen</li>' +
            //     '<li action="createNewFromTemplate">Als Template verwenden ...</li>' +
            // '</ul>' +
            '<span class="ui-state-default ui-corner-all" onclick="adminMaintenance.actionHandler(\'edit\', event.target)"><span class="btnUrl ui-icon ui-icon-pencil" title="Bearbeiten"></span></span>' +
        '</div>';
        
        function nl2br (str, is_xhtml) {
            var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';
            return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag + '$2');
        }
</script>

</head>
<body>

    <c:set var="activeTab" value="7" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

