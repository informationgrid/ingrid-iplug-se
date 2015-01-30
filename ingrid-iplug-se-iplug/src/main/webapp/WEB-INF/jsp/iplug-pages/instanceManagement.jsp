<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2015 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or â€“ as soon they will be
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
<title><fmt:message key="DatabaseConfig.main.title" /> - Management</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
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
<!--<script src="../js/chart.min.js"></script>-->
<script type="text/javascript" src="../js/jquery.tablesorter.full.min.js"></script>

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
        
        getStatistic();

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
                if (error === "Found") {
                    $("#crawlInfo").show();
                    $("#crawlStart").show();
                    $("#crawlStop").hide();
                    //$("#allInfo").hide();
                    var data = "";
                    if (jqXHR.responseText == "") {
                        $("#crawlInfo").html( "Es läuft zur Zeit kein Crawl." );
                        
                    } else {
                        $("#crawlInfo").html( "Es läuft zur Zeit kein Crawl. (<a href='#' onclick='$(\"#allInfo\").toggle()'>Information zum letzten Crawl) " );
                        data = JSON.parse( jqXHR.responseText );
                        setLog( data );
                        // show link to request hadoop.log content
                        $("#moreInfo").show();
                        if (statisticIsUpdated) getStatistic();
                    }
                    
                    // repeat execution every 60s until finished
                    setTimeout( checkState, 60000 );

                // when a real error occurs
                } else {
                    $("#crawlInfo").html( "Es trat ein Fehler beim Laden des Logs auf. " );
                    console.error( error, jqXHR );                  
                }
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
    
    function getStatistic() {
        statisticIsUpdated = false;
        // fill table
        var addTableRow = function(item, biggest) {
            var knownWidth = (item.known / biggest.known) * 100;
            var fetchedWidth = (item.fetched / item.known) * knownWidth;
            var toFetchWidth = knownWidth - fetchedWidth;
            $("#statisticTable tbody").append(
                    "<tr>" +
                        "<td title='rot=noch nicht analysiert; grün=analysiert'><span style='display: inline-block; background-color: green; height: 3px; width: " + fetchedWidth + "px'></span><span style='display: inline-block; background-color: red; height: 3px; width: " + toFetchWidth + "px'></span></td>" +
                        "<td>" + item.host + "</td>" +
                        "<td>" + item.known + "</td>" +
                        "<td>" + item.fetched + "</td>" +
                        "<td>" + item.ratio + "</td>" +
                    "</tr>"
            );
        };
        
        $.ajax( "../rest/status/${ instance.name }/statistic", {
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                if (!data) {
                    $("#statisticTable").hide();
                    $("#overallStatistic").hide();
                    return;
                } else {
                    $("#statisticTable").show();
                    $("#overallStatistic").show();
                }
                
                var json = JSON.parse( data );
                var overall = json.splice(0, 1);
                // var labels = [], dataKnown = [], dataFetched = [];
                
                // determine highest known and fetched values
                var biggest = { known: -1, fetched: -1 };
                $.each( json, function(index, item) {
                    if (item.known > biggest.known) biggest.known = item.known;
                    if (item.fetched > biggest.fetched) biggest.fetched = item.fetched;
                });
                
                // remove all rows first
                $("#statisticTable tbody tr").remove();
                $.each( json, function(index, item) {
                    // labels.push( item.host );
                    // dataKnown.push( item.known );
                    // dataFetched.push( item.fetched );
                    addTableRow( item, biggest );
                });
                

                $("#overallStatistic .known").text( overall[0].known );
                $("#overallStatistic .fetched").text( overall[0].fetched );

                /*var ctx = document.getElementById("myChart").getContext("2d");
                var data = {
                    labels: labels,
                    datasets: [
                        {
                            label: "Insgesamt",
                            fillColor: "rgba(220,220,220,0.5)",
                            strokeColor: "rgba(220,220,220,0.8)",
                            highlightFill: "rgba(220,220,220,0.75)",
                            highlightStroke: "rgba(220,220,220,1)",
                            data: dataKnown
                        },
                        {
                            label: "Erfasst",
                            fillColor: "rgba(151,187,205,0.5)",
                            strokeColor: "rgba(151,187,205,0.8)",
                            highlightFill: "rgba(151,187,205,0.75)",
                            highlightStroke: "rgba(151,187,205,1)",
                            data: dataFetched
                        }
                    ]
                };
                var myBarChart = new Chart(ctx).Bar(data);*/
                
                $("#statisticTable").tablesorter({
                    headers : { 0 : { sorter : false } },
                    sortList: [[0,0]], // sort first column ascending
                    widgets: ['zebra', 'filter'],
                    widgetOptions: {
                        filter_columnFilters: true,
                        filter_hideFilters: false,
                        // class name applied to filter row and each input
                        filter_cssFilter  : 'filtered',
                        pager_removeRows: false
                    }
                });
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

