<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
<title><fmt:message key="DatabaseConfig.main.title" /> - Management</title>
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />
<link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script src="../js/jquery.validate.min.js"></script>
<script src="../js/localization/messages_de.min.js"></script>

<script type="text/javascript">

    var dialog,
        statisticIsUpdated = true;

    $(document).ready(function() {
        $("#crawlInfo").html( "Hole Status ..." );
        checkState();
        $("#crawlStop").hide();
        $("#allInfo").hide();
        
        $("#formManagement").validate({
            //errorLabelContainer: $("#formManagement div.error"),
            highlight: function(element, errorClass, validClass) {
                $(element).parent().addClass(errorClass);
            },
            unhighlight: function(element, errorClass, validClass) {
                $(element).parent().removeClass(errorClass);
            },
            errorPlacement: function(error, element) {
                error.insertAfter( element.parent() );
            }
        });
        
        dialog = $("#dialog-hadoop").dialog({
            autoOpen : false,
            height : 750,
            width : 770,
            modal : true,
            buttons : {
                "Schliessen" : function() {
                    dialog.dialog("close");
                }
            }
        });
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
            if (row.value) {
                content += "<div class='" + row.classification.toLowerCase() + "'>" + formatTime(row.time) + " - [" + row.classification + "] " + row.value + "</div>";
            }
        }

        $("#status").html( content );
    }
    
    function checkState() {
        $.ajax( "../rest/status/${ instance.name }", {
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                if (data == "") {
                    $("#crawlInfo").html( "Es läuft zur Zeit kein Crawl." );
                    setTimeout( checkState, 60000 );
                    return;
                } else if (data.some(function(item) { return item.key === "FINISHED" || item.key === "ERROR" || item.key === "ABORT" })) {
                    $("#crawlInfo").html( "Es läuft zur Zeit kein Crawl. (<a href='#' onclick='$(\"#allInfo\").toggle()'>Information zum letzten Crawl) " );
                    setLog( data );
                    // show link to request hadoop.log content
                    $("#moreInfo").show();
                    $("#crawlStop").hide();
                    $("#crawlStart").show();
                    // repeat execution every 60s until finished
                    setTimeout( checkState, 60000 );
                    return;
                }
                $("#crawlInfo").hide();
                $("#crawlStart").hide();
                $("#moreInfo").hide();
                $("#crawlStop").show();
                
                setLog( data );
                $("#allInfo").show();
                statisticIsUpdated = true;
                
                // repeat execution every 5s until finished
                setTimeout( checkState, 5000 );
            },
            error: function(jqXHR, text, error) {
                // if it's not a real error, but just saying, that no process is running
                $("#crawlInfo").html( "Es trat ein Fehler beim Laden des Logs auf. " );
                console.error( error, jqXHR );                  
            }
        });
    }

    function showHadoopLog() {
        dialog.dialog("open");
        $("#dialog-hadoop .content").html( "wird geladen ..." );
        $.ajax( "../rest/status/${ instance.name }/hadoop", {
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                $("#dialog-hadoop .content").html( data.replace(/\n/g, "<br>") );
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

