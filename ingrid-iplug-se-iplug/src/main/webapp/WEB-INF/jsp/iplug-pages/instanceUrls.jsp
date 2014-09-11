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
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/chosen.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="../js/chosen.jquery.min.js"></script>

<script type="text/javascript">
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

			allInputs.each(function(index, element) {
				var value = $(element).val();
				if (value) data.metadata.push({ metaKey: value, metaValue: value });
			});
			
			if ( valid ) {
				$.ajax({
					type: "POST",
					url: "/rest/addUrl.json?instance=${instance.name}",
					contentType: 'application/json',
					data: JSON.stringify( data )
				})
			    .done(function( result ) {
			        alert( "Data posted: " + result );
			    });
		        //dialog.dialog( "close" );
		    }
			return valid;
		}
		
		function addLimitUrlValidator() {
			var valid = true;
			var url = $("#limitUrl").val();
			var data = "";
			
			var button = "<div><button class='btnUrl'>Bearbeiten</button><button class='select'>Weitere Optionen</button></div><ul style='position:absolute; padding-left: 0; min-width: 100px;''><li action='jsDelete'>Löschen</li><li>Testen</li></ul>";
			
			if ( valid ) {
        		var actionButton = $("#limitUrlTable tbody")
        		  .append("<tr>" +
       				 "<td>" + url + "</td>" +
       				 "<td>" + button + "</td>" +
       			  "</tr>")
       			  .find("tr .btnUrl");
        		
        		createActionButton( actionButton );
        		
        		dialog.data("urlDataObject").limitUrls.push( url );
        		
        		dialogLimit.dialog( "close" );
		    }
			return valid;
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
				"Erstellen und weitere anlegen ..." : addUrlValidator,
				"Erstellen" : addUrlValidator
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
                "Erstellen" : addLimitUrlValidator
            },
		});
		/* form = dialog.find("form").on("submit", function(event) {
			event.preventDefault();
			addUser();
		}); */

		$("#btnAddUrl").on("click", function() {
			dialog.dialog("open");
			dialog.data("urlDataObject", {
					limitUrls: [],
					excludeUrls: [],
					metadata: []
			});
		});

		$("#btnAddLimitUrl").on("click", function() {
			dialogLimit.dialog("open");
		});
		
		// hide all metadata types initially
		//$("#metadata span").hide();
		$('#dialog-form input[type=radio]').change(function() {
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
		
		function createActionButton( btn ) {
    		btn.button().click(function() {
    				alert("Running the last action");
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
    						switch (action) {
    						case "jsDelete":
    							var row = $(evt.target).parents("tr");
    							var url = row.children()[0].innerHTML;
    							
    							var limitUrls = dialog.data("urlDataObject").limitUrls;
    							// remove url from data object
    							limitUrls.splice(limitUrls.indexOf( url ));
    							// remove row from table
    							row.remove();
    						}
    					}
    					menu.hide();
    				});
    				return false;
    			}).parent().buttonset().next().hide().menu();			
		}
		
		createActionButton( $(".btnUrl") );
		

		$("#dialog-form select").chosen({width: "100%", disable_search_threshold: 5});
		
	});
</script>

</head>
<body>

    <c:set var="activeTab" value="2" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

