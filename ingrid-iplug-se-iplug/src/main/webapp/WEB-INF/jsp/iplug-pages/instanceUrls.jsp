<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/chosen.min.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="../js/chosen.jquery.min.js"></script>

<script type="text/javascript">
    var urlMaintenance = null;
	$(document).ready(function() {
		$("#urlTable").tablesorter({
			headers : {
				2 : {
					sorter : false
				}
			}
		});

		function addUrlValidator() {
			var valid = true;
			
			var data = dialog.data("urlDataObject");
			
			// add all values from the dialog to the data object wich will be sent to the server
			// the limit/exclude urls already have been added
			data.url = $("#startUrl").val();
			
			data.metadata = [];
			
			// get metadata from all selects
			// get metadata from all visible checkboxes
			var allInputs = $("#dialog-form select, #dialog-form input[type=checkbox]:checked:visible");

			var handleMetadataItem = function(item) {
				var value = item.split(":");
				if (value.length === 2) {
				    data.metadata.push({ metaKey: value[0], metaValue: value[1] });					
				} else {
					console.error( "Value has wrong format: " + item );
				}
			}
			
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
			
			$.each( data.userMetadata, function(index, item) {
				handleMetadataItem( item );
			});
			
			delete data.userMetadata;
			
			if ( valid ) {
				$.ajax({
					type: "POST",
					url: "/rest/addUrl.json?instance=${instance.name}",
					contentType: 'application/json',
					data: JSON.stringify( data ),
					success: function() {
						// let JSP do the magic to refresh the page correctly
						location.reload();
					},
					error: function(jqXHR, text, error) {
						log.error(text, error);
					}
				});
		        
				dialog.dialog( "close" );
		    }
			return valid;
		}
		
		function getButtonTemplate(type) {
			var button = null;
		    switch(type) {
		    case "limitUrlTable":
		        button = "<div><button class='btnUrl'>Bearbeiten</button><button class='select'>Weitere Optionen</button></div><ul style='position:absolute; padding-left: 0; min-width: 100px;''><li action='jsDeleteLimit'>Löschen</li><li>Testen</li></ul>";
		    	break;
		    case "excludeUrlTable":
		        button = "<div><button class='btnUrl'>Bearbeiten</button><button class='select'>Weitere Optionen</button></div><ul style='position:absolute; padding-left: 0; min-width: 100px;''><li action='jsDeleteExclude'>Löschen</li><li>Testen</li></ul>";
		    	break;
		    case "userMetadataTable":
		        button = "<div><button type='button' onclick='urlMaintenance.deleteMetadata(event)'>Löschen</button>";
		    	break;
		    }
		    return button;
		}
		
		function addUrlRowTo(id, url) {
			var button = getButtonTemplate( id );
			
			var actionButton = $("#" + id + " tbody")
            .append("<tr>" +
               "<td>" + url + "</td>" +
               "<td>" + button + "</td>" +
            "</tr>")
            .find("tr .btnUrl");
          
			if (id !== "userMetadataTable") {
			    createActionButton( actionButton );
			}
		}
		
		function addLimitUrlValidator(type) {
			var valid = true;
			var url = $("#limitUrl").val();
			if (type === "exclude") {
			    url = $("#excludeUrl").val();
			}
			
			if ( valid ) {
				if (type === "limit") {
					addUrlRowTo("limitUrlTable", url);
					dialog.data("urlDataObject").limitUrls.push( url );
                	dialogLimit.dialog( "close" );
				} else {
					addUrlRowTo("excludeUrlTable", url);
                    dialog.data("urlDataObject").excludeUrls.push( url );
                	dialogExclude.dialog( "close" );
				}
        		
		    }
			return valid;
		}
		
		function resetFields() {
			// reset all select boxes
			$("#dialog-form select").each(function(index, item) {
				$(item).val("");
				$(item).trigger("chosen:updated");
			});
			
			// empty tables
			$("#dialog-form tbody tr").remove();
			
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
    				$("#startUrl").val( data.url ? data.url : "http://" );
    				$.each( data.limitUrls, function(index, url) {
    					addUrlRowTo("limitUrlTable", url);
    				});
    				$.each( data.excludeUrls, function(index, url) {
    					addUrlRowTo("excludeUrlTable", url);
    				});
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
    				var additionalMetadata = [];
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
                    	addUrlRowTo( "userMetadataTable", value );
                    });
    			}
			},
			close : function() {}
		});

		dialogLimit = $("#dialog-form-limit").dialog({
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
		});
		/* form = dialog.find("form").on("submit", function(event) {
			event.preventDefault();
			addUser();
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

		//settings = "${partners}";
		// only show providers that belong to their partner! 
		/* $("#provider option").hide();
		$("#provider ." + $("#partner").val()).show();
		$("#partner").on("change", function(event) {
			var partnerShort = $("#partner").val();
			$("#provider option").hide();
			$("#provider ." + partnerShort).show();
			// select first one
			$("#provider").val( $("#provider ." + partnerShort)[0].value );
		}); */
		
		function deleteMetadata(evt) {
			actionHandler( "jsDeleteUserMetadata", $(evt.target) );
		}
		
		function actionHandler( action, target ) {
			var type = null;
			switch (action) {
            case "jsDeleteUserMetadata":
            	type = type ? type : "userMetadata";
                // FALL THROUGH!!!
            case "jsDeleteExclude":
            	type = type ? type : "excludeUrls";
                // FALL THROUGH!!!
            case "jsDeleteLimit":
                type = type ? type : "limitUrls";
                var row = target.parents("tr");
                var url = row.children()[0].innerHTML;
                
                var dataArray = dialog.data("urlDataObject")[ type ];
                // remove url from data object
                dataArray.splice(dataArray.indexOf( url ));
                // remove row from table
                row.remove();
            }
		}
		
		function createActionButton( btn ) {
    		btn.button().click(function() {
    			var id = this.getAttribute("data-id");
    			$.get("/rest/url/" + id, function(data) {
    				console.log("Data: ", data);
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
		
		createActionButton( $(".btnUrl") );

		$("#btnAddUrl").on("click", function() {
            dialog.data("urlDataObject", {
                    limitUrls: [],
                    excludeUrls: [],
                    metadata: [],
                    userMetadata: []
            });
            dialog.dialog("open");
        });

        $( "#btnAddLimitUrl" ).on( "click", function() {
            dialogLimit.dialog("open");
        });
        $( "#btnAddExcludeUrl" ).on( "click", function() {
            dialogExclude.dialog("open");
        });
        
        $("#userMetaError").hide();
        $("#btnAddUserMetadata").on( "click", function() {
            var newMeta = $( "#userMeta" ).val();
            if (newMeta.split(":").length === 2) {
                $("#userMetaError").hide();
                $( "#userMeta" ).val( "" );
                addUrlRowTo( "userMetadataTable", newMeta );
                dialog.data( "urlDataObject" ).userMetadata.push( newMeta );
            } else {
                $("#userMetaError").show();
            }
        });

		$("#dialog-form select").chosen({width: "100%", disable_search_threshold: 5});
		$("#filterMetadata").chosen({width: "100%", disable_search_threshold: 5})
		    .change(function(event, options) {
		    	var filter = [];
		    	$.each(this.selectedOptions, function(index, option) {
		    		filter.push( option.getAttribute("value") ); 
		    	});
		        
		        location.search = "?instance=${instance.name}&filter=" + filter.join(",");
		    });
		
    	/* PUBLIC FUNCTIONS */
    	urlMaintenance = {
    			deleteMetadata: deleteMetadata
    	};
	});
	
	
</script>

</head>
<body>

    <c:set var="activeTab" value="2" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

