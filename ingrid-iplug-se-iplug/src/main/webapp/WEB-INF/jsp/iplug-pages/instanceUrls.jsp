<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2025 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.2 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
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
<title><fmt:message key="DatabaseConfig.main.title" /> - Url-Pflege</title>
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
            output: '{startRow} bis {endRow} von {filteredRows} URLs',
            //size: 2,
            savePages : false,
            ajaxUrl : '../rest/urls/${instance.name}?page={page}',
            serverSideSorting: true,
            ajaxObject: {
                dataType: 'json'
            },
            ajaxProcessing: function(data){
                if (data) {// && data.hasOwnProperty('rows')) {
                    var r, d = data.data,
                    // total number of rows (required)
                    total = data.totalUrls,
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
                                "<td>" + d[r].url + " <a target='_blank' href='" + d[r].url + "' title='Link öffnen'><span class='ui-icon ui-icon-extlink' style='display:inline-block;'></span></a></td>" +
                                "<td>" + d[r].status + "</td>" +
                                "<td>" + actionButtonTemplate + "</td>" +
                                "</tr>";

                        //rows.push(row); // add new row array to rows array

                        // add another child row for metadata
                        var excludeUrlsLine = d[r].excludeUrls.length > 0 ? "<div class='exclude'>Exclude-URLs: " + d[r].excludeUrls.join(',') + "</div>" : "";
                        var metadata = [];
                        d[r].metadata.forEach( function(m) { metadata.push( m.metaKey + ":" + m.metaValue ); });
                        rows += "<tr class='tablesorter-childRow'><td></td><td colspan='3' class='url-info'>" +
                                "<div class='limit'>Limit-URLs: " + d[r].limitUrls.join(',') + "</div>" +
                                excludeUrlsLine +
                                "<div class='metadata'>Metadaten: " + metadata.join(',') + "</div>" +
                                "</td></tr>";
                    }
                    //setTimeout( function() { createActionButton( $("tr .btnUrl") ); }, 0);

                    // in version 2.10, you can optionally return $(rows) a set of table rows within a jQuery object
                    return [ total, $(rows) ];
                }
            },
            customAjaxUrl: function(table, url) {
                // manipulate the url string as you desire
                var metafilter = $("#urlTable").data().metafilter;
                var urlfilter = $("#urlTable").data().urlfilter;
                var sort = $('#urlTable').data().tablesorter.sortList[0];

                if (metafilter) url += "&metafilter=" + metafilter;
                if (urlfilter) url += "&urlfilter=" + urlfilter;
                if (sort) url += "&sort=" + sort.join(',');
                url += "&pagesize=" + this.size;

                return encodeURI(url);
            }
        };

        function isUrl( url ) {
            return (/^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:[/?#]\S*)?$/i.test( url ));
        }

        function addUrlValidator() {
            var valid = true;
            $(".error").hide();

            var data = dialog.data("urlDataObject");

            // add all values from the dialog to the data object wich will be sent to the server
            // the limit/exclude urls already have been added
            data.url = $("#startUrl").val();

            // check if url is valid
            if ( !isUrl( data.url ) ) {
                $(".error.startUrl").show();
                valid = false;
            }
            
            // automatically add limit/exclude URLs if forgotten
            $('#btnAddLimitUrl').click();
            $('#btnAddExcludeUrl').click();

            // ************************************
            // Check if limitUrls are valid and set
            // ************************************
            $.each( data.limitUrls, function(index, url) {
            	var localValid = true;
            	if (url.indexOf("/") === 0 && url.lastIndexOf("/") === (url.length - 1)) {
            		var innerUrl = url.substring(1, url.length - 1);
                    if ( !isUrl( innerUrl ) ) {
                    	localValid = false;
                    } else {
                    	try {
                    		var regex = new RegExp(innerUrl, "g");
                    	} catch (e) {
                        	localValid = false;
                    	}
                    	var parser = document.createElement('a');
                    	parser.href = innerUrl;
                        if (innerUrl.indexOf(parser.host+"/")  == -1 && parser.host.indexOf("xn--") == -1 ) {
                        	localValid = false;
                        }
                    }
            	} else {
                    if ( !isUrl( url ) ) {
                    	localValid = false;
                    }
            	}
            	if (!localValid ) {
                	$("#limitUrlTable_" + index).css('border','1px solid red');
                    $("#errorLimitUrl").show();
                    valid = false;
            	}
            });

            // ************************************
            // Check if excludeUrls are valid and set
            // ************************************
            $.each( data.excludeUrls, function(index, url) {
            	var localValid = true;
            	if (url.indexOf("/") === 0 && url.lastIndexOf("/") === (url.length - 1)) {
            		var innerUrl = url.substring(1, url.length - 1);
                    if ( !isUrl( innerUrl ) ) {
                    	localValid = false;
                    } else {
                    	try {
                    		var regex = new RegExp(innerUrl, "g");
                    	} catch (e) {
                        	localValid = false;
                    	}
                    	var parser = document.createElement('a');
                    	parser.href = innerUrl;
                        if (innerUrl.indexOf(parser.host+"/")  == -1 ) {
                        	localValid = false;
                        }
                    }
            	} else {
                    if ( !isUrl( url ) ) {
                    	localValid = false;
                    }
            	}
            	if (!localValid ) {
                	$("#excludeUrlTable_" + index).css('border','1px solid red');
                    $("#errorExcludeUrl").show();
                    valid = false;
            	}
            });
            
            // if no limit url has been set, then take the start url if it was valid(!)
            if (valid && data.limitUrls.length === 0) {
                data.limitUrls.push( data.url );
            }
            
            data.metadata = [];

            // get metadata from all selects
            // get metadata from all visible checkboxes
            var allInputs = $("#dialog-form select, #dialog-form input[type=checkbox]:checked:visible");

            // ************************************
            // Check if metadata is in valid format
            // ************************************
            var handleMetadataItem = function(item) {
                var value = item.split(":");
                if (value.length === 2) {
                    data.metadata.push({ metaKey: value[0], metaValue: value[1] });
                } else {
                    console.error( "Value has wrong format: " + item );
                }
            };

            allInputs.each(function(index, element) {
                var val = $(element).val();


                if (val instanceof Array) {
                    $.each(val, function(index, meta) {
                        handleMetadataItem( meta );
                    });

                } else if (val !== null) {
                        handleMetadataItem( val );

                } else {
                    // value was not chosen
                    // -> check if value is required
                }
            });

            if (data.userMetadata) {
                $.each( data.userMetadata, function(index, item) {
                    handleMetadataItem( item );
                });
            }

            if ( valid ) {
                delete data.userMetadata;
                $("#waitScreen").show();
                $.ajax({
                    type: "POST",
                    url: "../rest/url",
                    contentType: 'application/json',
                    data: JSON.stringify( data ),
                    success: function() {
                        // let JSP do the magic to refresh the page correctly
                        location.reload();
                    },
                    error: function(jqXHR, text, error) {
                        console.error(text, error);
                        $("#waitScreen").hide();
                    }
                });

                dialog.dialog( "close" );
            }
            return valid;
        }

        function getButtonTemplate(type) {
            var button = null;
            /* switch(type) {
            case "limitUrlTable":
                button = "<div><button class='btnUrl'>Bearbeiten</button><button class='select'>Weitere Optionen</button></div><ul style='position:absolute; padding-left: 0; min-width: 100px;''><li action='jsDeleteLimit'>Löschen</li><li>Testen</li></ul>";
                break;
            case "excludeUrlTable":
                button = "<div><button class='btnUrl'>Bearbeiten</button><button class='select'>Weitere Optionen</button></div><ul style='position:absolute; padding-left: 0; min-width: 100px;''><li action='jsDeleteExclude'>Löschen</li><li>Testen</li></ul>";
                break;
            case "userMetadataTable":
                button = "<div><button type='button' onclick='urlMaintenance.deleteMetadata(event)'>Löschen</button>";
                break;
            } */
            button = "<div><button type='button' onclick='urlMaintenance.deleteRow(\"" + type + "\", event)'>Löschen</button>";
            return button;
        }

        function addUrlRowTo(id, url, pos) {
            var button = getButtonTemplate( id );

            // var actionButton = $("#" + id + " tbody .newRow");
            var newRow = $(
                "<tr data-row='" + pos + "'>" +
                   "<td id='" + id + "_" + pos + "'>" + url + "</td>" +
                   "<td data-editable='false'>" + button + "</td>" +
                "</tr>"
                   
            );
            newRow.insertBefore( $("#" + id + " tbody .newRow") );
            //.find("tr .btnUrl");

            // make tables limit/exclude editable
            $("#" + id).editableTableWidget();

            $('td', newRow).on(
                'change',
                function(evt, newValue) {
                    var row = evt.target.parentNode;
                    var rowPos = row.getAttribute( "data-row" );
                    if ( id === "limitUrlTable" ) {
                        dialog.data("urlDataObject").limitUrls[ rowPos ] = newValue;

                    } else {
                        dialog.data("urlDataObject").excludeUrls[ rowPos ] = newValue;
                    }
                }
            );

            if (id !== "userMetadataTable") {
                //createActionButton( actionButton );
            }
        }

        /* function addLimitUrlValidator(type) {
            var valid = true;
            var url = $("#limitUrl").val();
            if (type === "exclude") {
                url = $("#excludeUrl").val();
            }

            if ( valid ) {
                if (type === "limit") {
                    addUrlRowTo("limitUrlTable", url, dialog.data("urlDataObject").limitUrls.length);
                    dialog.data("urlDataObject").limitUrls.push( url );
                    dialogLimit.dialog( "close" );
                } else {
                    addUrlRowTo("excludeUrlTable", url, dialog.data("urlDataObject").excludeUrls.length);
                    dialog.data("urlDataObject").excludeUrls.push( url );
                    dialogExclude.dialog( "close" );
                }

            }
            return valid;
        } */

        function resetFields() {
            if (dialog.data("isNew") === true) {
                $(".ui-dialog-title", dialog.parent()).text( "Neue URL anlegen" );
                $("#dialog-form").next().find("button:last .ui-button-text").text( "Erstellen" );

            } else {
                $(".ui-dialog-title", dialog.parent()).text( "URL bearbeiten" );
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
            height : 750,
            width : 770,
            modal : true,
            buttons : {
                "Abbrechen" : function() {
                    dialog.dialog("close");
                },
                /* "Erstellen und weitere anlegen ..." : addUrlValidator, */
                "Erstellen" : addUrlValidator
            },
            open: function() {
                // reset all fields first
                resetFields();

                var data = dialog.data("urlDataObject");
                if (data) {
                    $("#startUrl").val( data.url ? data.url : "https://" );
                    $.each( data.limitUrls, function(index, url) {
                        addUrlRowTo("limitUrlTable", url, index);
                    });
                    $.each( data.excludeUrls, function(index, url) {
                        addUrlRowTo("excludeUrlTable", url, index);
                    });

                    // make tables limit/exclude editable
                    /* $('#limitUrlTable, #excludeUrlTable').editableTableWidget({
                        //editor : $('<textarea>')
                    });

                    $('#limitUrlTable tr:not(.newRow) td').on(
                        'change',
                        function(evt, newValue) {
                            var row = evt.target.parentNode;
                            var rowPos = row.getAttribute( "data-row" );
                            dialog.data("urlDataObject").limitUrls[ rowPos ] = newValue;
                        }
                    ); */

                    // a metadata consists of a key and a value which is represented as
                    // a key:value option inside an element with the id of the key
                    // collect all metadata with the same key to do multiselection
                    var metaMap = {};
                    $.each( data.metadata, function(index, meta) {
                        if (!metaMap[ meta.metaKey ]) {
                            metaMap[ meta.metaKey ] = [];
                        }
                        metaMap[ meta.metaKey ].push( meta.metaKey + ":" + meta.metaValue );
                    });

                    $.each( metaMap, function(key, values) {
                        var elem = $("#" + key);
                        // if there's an element responsible for the metadata
                        if (elem) {
                            // update selected data
                            elem.val(values);
                            elem.trigger("chosen:updated");

                            // find values not available as option, which will be added to user-defined list
                            $.each(values, function(index, value) {
                                if ($("option[value='"+value+"']").length === 0) {
                                    data.userMetadata.push( value );
                                }
                            });

                        // otherwise add to user-defined metadata
                        } else {
                            $.each(values, function(index, value) {
                                data.userMetadata.push( value );
                            });
                        }
                    });

                    $.each( data.userMetadata, function(index, value) {
                        addUrlRowTo( "userMetadataTable", value, index );
                    });
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
                    if (action.type === "deleteSingleUrl") {
                        $.ajax({
                            url: "../rest/url/" + action.id,
                            type: 'DELETE',
                            success: function() {
                                location.reload();
                            },
                            data: action.id,
                            contentType: 'application/json'
                        });
                    } else if (action.type === "deleteMultipleUrls") {
                        var checkedRows = $( "#urlTable input:checked" );
                        var dataIDs = [];
                        checkedRows.each( function(index, row) {
                            dataIDs.push( $( row ).parents("tr").attr("data-id") );
                        });
                        $.ajax({
                            type: "DELETE",
                            url: "../rest/urls",
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

        dialogTestResult = $( "#dialog-testresult" ).dialog({
            resizable: true,
            autoOpen: false,
            height:400,
            width:500,
            modal: true,
            buttons: {
                "Schliessen": function() {
                    $( this ).dialog( "close" );
                }
            },
            open: function() {
                $("#dialog-testresult .loading").show();
                $("#dialog-testresult .result").hide();
            }
        });

/*      dialogLimit = $("#dialog-form-limit").dialog({
            autoOpen : false,
            height : 250,
            width : 550,
            modal : true,
            buttons : {
                "Abbrechen" : function() {
                    dialogLimit.dialog("close");
                },
                "Erstellen" : function() {
                    addLimitUrlValidator("limit")
                }
            },
        });
        dialogExclude = $("#dialog-form-exclude").dialog({
            autoOpen : false,
            height : 250,
            width : 550,
            modal : true,
            buttons : {
                "Abbrechen" : function() {
                    dialogExclude.dialog("close");
                },
                "Erstellen" : function() {
                    addLimitUrlValidator("exclude")
                }
            },
        }); */

        // hide all metadata types initially
        //$("#metadata span").hide();
        $( '#dialog-form input[type=radio]' ).change(function() {
            var parent = this.parentNode.parentNode;
            // hide all
            $("fieldset", parent).hide();
            // only show corresponding values
            $("fieldset.meta_" + this.value).show();
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
                $.get("../rest/url/" + id, function(data) {
                    // reload page if data was not received, which calls login page
                    if (typeof data === "string") location.reload();
                    console.log("Data: ", data);
                    dialog.data("isNew", false);
                    data.userMetadata = [];
                    dialog.data("urlDataObject", data);
                    dialog.dialog("open");
                });
                break;
            case "createNewFromTemplate":
                $.get("../rest/url/" + id, function(data) {
                    // reload page if data was not received, which calls login page
                    if (typeof data === "string") location.reload();
                    // empty start/limit/exclude URL(s) and remove the id
                    delete data.id;
                    delete data.status;
                    data.userMetadata = [];
                    data.url = "http://";
                    data.limitUrls = [];
                    data.excludeUrls = [];
                    dialog.data("urlDataObject", data);
                    dialog.data("isNew", true);
                    dialog.dialog("open");
                });
                break;
            case "userMetadataTable":
                type = type ? type : "userMetadata";
                // FALL THROUGH!!!
            case "excludeUrlTable":
                type = type ? type : "excludeUrls";
                // FALL THROUGH!!!
            case "limitUrlTable":
                type = type ? type : "limitUrls";
                var row = target.parents("tr");
                var url = row.children()[0].innerHTML;

                var dataArray = dialog.data("urlDataObject")[ type ];
                // remove url from data object
                dataArray.splice(dataArray.indexOf( url ), 1);
                // remove row from table
                row.remove();
                break;
            case "delete":
                dialogConfirm.data("action", {
                    type: "deleteSingleUrl",
                    id: id
                });
                dialogConfirm.dialog("open");
                break;
            case "test":
                var url = $( target ).parents("tr").find("td:nth-child(2)").text();
                dialogTestResult.dialog("open");
                $.ajax({
                    type: "POST",
                    url: "../rest/url/${instance.name}/check",
                    contentType: 'application/json',
                    data: url,
                    success: function(data) {
                        $("#dialog-testresult .result").html( nl2br(data) );
                        $("#dialog-testresult .loading").hide();
                        $("#dialog-testresult .result").show();
                    },
                    error: function(jqXHR, text, error) {
                        console.error(text, error);
                    }
                });
                break;
            default:
                alert( "Unbekannte Aktion: ", action );
            }
        }

        function createActionButton( btn ) {
            btn.button().click(function() {
                var id = $( this ).parents("tr").attr("data-id");
                $.get("../rest/url/" + id, function(data) {
                    // reload page if data was not received, which calls login page
                    if (typeof data === "string") location.reload();
                    console.log("Data: ", data);
                    dialog.data("isNew", false);
                    data.userMetadata = [];
                    dialog.data("urlDataObject", data);
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

        $("#btnAddUrl").on("click", function() {
            dialog.data("urlDataObject", {
                    instance: '${ instance.name }',
                    limitUrls: [],
                    excludeUrls: [],
                    metadata: $.extend({}, defaultValues), // add a copy of default values
                    userMetadata: []
            });
            dialog.data("isNew", true);
            dialog.dialog("open");
        });

        $( "#btnAddLimitUrl" ).on( "click", function() {
            //dialogLimit.dialog("open");
            var url = $("#newLimitUrl").val();
            if (url.trim() !== "") {
                addUrlRowTo( "limitUrlTable", url, dialog.data("urlDataObject").limitUrls.length );
                dialog.data("urlDataObject").limitUrls.push( url );
                $("#newLimitUrl").val("");
            }
        });
        $( "#btnAddExcludeUrl" ).on( "click", function() {
            //dialogExclude.dialog("open");
            var url = $("#newExcludeUrl").val();
            if (url.trim() !== "") {
                addUrlRowTo( "excludeUrlTable", url, dialog.data("urlDataObject").excludeUrls.length );
                dialog.data("urlDataObject").excludeUrls.push( url );
                $("#newExcludeUrl").val("");
            }
        });

        // action for button to add user metadata
        $("#userMetaError").hide();
        $("#btnAddUserMetadata").on( "click", function() {
            var newMeta = $( "#userMeta" ).val();
            if (newMeta.split(":").length === 2) {
                $("#userMetaError").hide();
                $( "#userMeta" ).val( "" );
                addUrlRowTo( "userMetadataTable", newMeta, dialog.data( "urlDataObject" ).userMetadata.length );
                dialog.data( "urlDataObject" ).userMetadata.push( newMeta );
            } else {
                $("#userMetaError").show();
            }
        });

        // action for button to delete urls
        $("#btnDeleteUrls").on( "click", function() {
            dialogConfirm.data( "action", {
                type: "deleteMultipleUrls"
            });
            dialogConfirm.dialog( "open" );
        });

        // convert select boxes to better ones
        var chosenOptions = {
            width: "100%",
            disable_search_threshold: 5,
            placeholder_text_multiple: "Bitte auswählen",
            no_results_text: "Keinen Eintrag gefunden"
        };

        var updateBrowserHistory = function() {
        	
            // avoid problem with missing functionality in IE9
            if (window.history.pushState)  {
                window.history.pushState(null, null, location.pathname + "?instance=${instance.name}&urlfilter=" + $("#urlTable").data().urlfilter + "&filter=" + $("#urlTable").data().metafilter);
            }
        };

        var setFilterValues = function() {
            var filter = [];
            var options = $("#filterMetadata option:selected");
            $.each(options, function(index, option) {
                filter.push( option.getAttribute("value") );
            });
            var filterParam = filter.join(",");
            var value = $("#filterUrl").val();

            $("#urlTable").data().metafilter = filterParam;
            $("#urlTable").data().urlfilter = value;

            var filterString = filterParam+value;
            // workaround to trigger search filter correctly if resetting a filter
            if (filterString === "") filterString = " ";

            // use a combination of both filters to initiate a new search
            // since we prepare the final url with request parameters ourselves
            // it doesn't matter which value we trigger the search with
            $('#urlTable').trigger('search', [[ filterString ]]);
            updateBrowserHistory();
        };

        $("#dialog-form select").chosen( chosenOptions );
        $("#filterMetadata").chosen( chosenOptions )
            .change(function() {
                setFilterValues();
            });

        // filter by Url
        $("#filterUrl").on( "keyup", function() {
            setFilterValues();
        });

        setFilterValues();

        // initialize the table and its paging option
        $("#urlTable").tablesorter({
            headers : {
                0 : {
                    sorter : false
                },
                3 : {
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

        $("#urlContent").css("visibility", "visible");
        $("#loading").hide();


        /* PUBLIC FUNCTIONS */
        urlMaintenance = {
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
            '<span class="ui-state-default ui-corner-all" onclick="urlMaintenance.actionHandler(\'edit\', event.target)"><span class="btnUrl ui-icon ui-icon-pencil" title="Bearbeiten"></span></span>' +
            '<span class="ui-state-default ui-corner-all" onclick="urlMaintenance.actionHandler(\'createNewFromTemplate\', event.target)"><span class="btnUrl ui-icon ui-icon-newwin" title="Als Template verwenden ..."></span></span>' +
            '<span class="ui-state-default ui-corner-all" onclick="urlMaintenance.actionHandler(\'test\', event.target)"><span class="btnUrl ui-icon ui-icon-transfer-e-w" title="Testen"></span></span>' +
        '</div>';
        
        function nl2br (str, is_xhtml) {
            var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';
            return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag + '$2');
        }
</script>

</head>
<body>

    <c:set var="activeTab" value="2" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

