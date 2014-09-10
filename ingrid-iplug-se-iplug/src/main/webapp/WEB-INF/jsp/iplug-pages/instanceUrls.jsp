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
			
			// $("input[name=type]:checked").val()
			
			if ( valid ) {
        		$("#urlTable tbody").append("<tr>" +
       				"<td>" + url + "</td>" +
       				"<td>" + data + "</td>" +
       			"</tr>");
		        dialog.dialog( "close" );
		    }
			return valid;
		}
		
		function addLimitUrlValidator() {
			var valid = true;
			var url = $("#limitUrl").val();
			var data = "";
			
			if ( valid ) {
        		$("#limitUrlTable tbody")
        		  .append("<tr>" +
       				 "<td>" + url + "</td>" +
       				 "<td>" + data + "</td>" +
       			  "</tr>")
       			  .find("tr")
                  .contextMenu(menu, {
                	    triggerOn: 'contextmenu',
                	    afterOpen: function(data, trigger) {
                	        clickedTarget = trigger.target;
                	    } 
       		      });
        		dialogLimit.dialog( "close" );
		    }
			return valid;
		}

		dialog = $("#dialog-form").dialog({
			autoOpen : false,
			height : 750,
			width : 750,
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
			height : 400,
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
		});

		$("#btnAddLimitUrl").on("click", function() {
			dialogLimit.dialog("open");
		});
		
		// hide all metadata types initially
		$("#metadata span").hide();
		$('input[type=radio][name=type]').change(function() {
			// hide all
    		$("#metadata span").hide();
			// only show corresponding values
    		$("#metadata span." + this.value).show();
		});

		//settings = "${partners}";
		// only show providers that belong to their partner! 
		$("#provider option").hide();
		$("#provider ." + $("#partner").val()).show();
		$("#partner").on("change", function(event) {
			var partnerShort = $("#partner").val();
			$("#provider option").hide();
			$("#provider ." + partnerShort).show();
			// select first one
			$("#provider").val( $("#provider ." + partnerShort)[0].value );
		});
		
		$(".btnUrl").button().click(function() {
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
				$(document).one("click", function() {
					menu.hide();
				});
				return false;
			}).parent().buttonset().next().hide().menu();

		//$("#provider").chosen({width: "100%"});
		
	});
</script>

</head>
<body>

    <c:set var="activeTab" value="2" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

