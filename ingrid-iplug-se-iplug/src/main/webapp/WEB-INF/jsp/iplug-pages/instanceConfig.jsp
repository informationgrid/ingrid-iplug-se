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
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.easytabs.min.js"></script>
<script type="text/javascript" src="../js/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="../js/mindmup-editabletable.js"></script>

<script type="text/javascript">
	$(document).ready(
		function() {
			/* $('#tab-container').easytabs({
				animate : false
			});

			$('#schedulingTabs').easytabs({
				animate : false
			}); */

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
    
    <c:set var="activeTab" value="1" scope="request"/>
	<c:import url="includes/body.jsp"></c:import>
    
</body>
</html>
