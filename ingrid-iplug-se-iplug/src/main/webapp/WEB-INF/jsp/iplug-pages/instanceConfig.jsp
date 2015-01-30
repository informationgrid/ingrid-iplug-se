<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> - Instanzkonfiguration</title>
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
			$('#configTabs').easytabs({
				animate : false
			});
			
			$("#btnUpdateMetadata").on( "click", function() {
    			$.ajax({
                    type: "POST",
                    url: "../rest/updateMetadata?instance=${instance.name}",
                    contentType: 'application/json',
                    data: $("#metadata").val(),
                    success: function() {
                        // let JSP do the magic to refresh the page correctly
                        location.reload();
                    },
                    error: function(jqXHR, text, error) {
                        console.error(text, error);
                        alert(error);
                    }
                });
			});

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
