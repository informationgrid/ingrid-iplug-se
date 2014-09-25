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

<script type="text/javascript">

	
	$(document).ready(function() {
        $("#crawlInfo").html( "Hole Status ..." );
    	checkState();
        $("#crawlStop").hide();
	});
	
	function setLog(data) {
		var formatTime = function(ts) {
            var date = new Date(ts);
            var d = date.getDate();
            var m = date.getMonth() + 1;
            var y = date.getFullYear();
            var time = date.toTimeString().substring(0, 8);
            return '' + y + '-' + (m<=9 ? '0' + m : m) + '-' + (d <= 9 ? '0' + d : d) + ' ' + time;
        };
        
        // fill div with data from content
        var content = "";
        for (var i=0; i < data.length; i++) {
            var row = data[i];
            content += "<div class='" + row.classification.toLowerCase() + "'>" + formatTime(row.time) + " - [" + row.classification + "] " + row.value + "</div>";                 
        }
        $("#status").html( content );
	}
	
	function checkState() {
    	$.ajax( "../rest/status/${ instance.name }", {
    		type: "GET",
            contentType: 'application/json',
            success: function(data) {
                if (!data) {
                	$("#crawlInfo").html( "Es läuft zur Zeit kein Crawl." );
                    $("#crawlStart").show();
                    $("#crawlStop").hide();
                    return;
                }
                $("#crawlInfo").hide();
                $("#crawlStart").hide();
                $("#crawlStop").show();
                
                setLog( data );
                $("#status").show();
                
                // repeat execution every 5s until finished
                setTimeout( checkState, 5000 );
            },
            error: function(jqXHR, text, error) {
            	if (error === "Found") {
            		$("#crawlInfo").html( "Es läuft zur Zeit kein Crawl. (<a href='#' onclick='$(\"#status\").toggle()'>Information zum letzten Crawl) " );
            		$("#status").hide();
            		$("#crawlStart").show();
                    $("#crawlStop").hide();
            		var data = JSON.parse( jqXHR.responseText );
                    setLog( data );
            	} else {
            		$("#crawlInfo").html( "Es trat ein Fehler beim Laden des Logs auf. " );
                    console.error( error, jqXHR );            		
            	}
            }
    	});
	}
	
</script>

</head>
<body>

    <c:set var="activeTab" value="4" scope="request" />
    <c:import url="includes/body.jsp"></c:import>

</body>
</html>

