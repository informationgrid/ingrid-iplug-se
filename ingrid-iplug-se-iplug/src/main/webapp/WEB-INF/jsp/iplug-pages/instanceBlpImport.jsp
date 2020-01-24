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
    <title><fmt:message key="DatabaseConfig.main.title"/>
      - BLP Import</title>
    <meta name="description" content=""/>
    <meta name="keywords" content=""/>
    <meta name="author" content="wemove digital solutions"/>
    <meta name="copyright" content="wemove digital solutions GmbH"/>
    <link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css" media="all"/>
    <link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all"/>
    <link rel="StyleSheet" href="../css/se_styles.css" type="text/css" media="all"/>

    <script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
    <script type="text/javascript" src="../js/jquery-ui.min.js"></script>
    <script src="../js/jquery.validate.min.js"></script>
    <script src="../js/localization/messages_de.min.js"></script>

    <script type="text/javascript">
      var dialog;

      $(document).ready(function () {
        $("#allInfo").hide();
        dialog = $("#dialog-detailed").dialog({
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

        checkState();
      });

      function checkState() {
          $.ajax( "../rest/status/${ instance.name }/blpimport", {
              type: "GET",
              contentType: 'application/json',
              success: function(data) {
                  if (data == "") {
                      $("#importInfo").html( "Es läuft zur Zeit kein Import." );
                      setTimeout( checkState, 60000 );
                      return;
                  } else if (data.some(function(item) { return item.key === "FINISHED" || item.key === "ERROR" || item.key === "ABORT" })) {
                      var lastTime = formatTime(data[data.length-1].time);
                      $("#importInfo").html( "Es läuft zur Zeit kein Import.</br>Letzter Import: "+ lastTime +"  (<a href='#' onclick='$(\"#allInfo\").toggle()'>Informationen) " );
                      setLog( data );
                      $("#moreInfo").show();
                      // repeat execution every 60s until finished
                      setTimeout( checkState, 60000 );
                      return;
                  }
                  setLog( data );
                  $("#statusContent").show();
                  $("#importInfo").hide();
                  $("#moreInfo").hide();
                  $("#allInfo").show();


                  // repeat execution every 5s until finished
                  setTimeout( checkState, 5000 );
              },
              error: function(jqXHR, text, error) {
                  // if it's not a real error, but just saying, that no process is running
                  $("#statusContent").html( "Es trat ein Fehler beim Laden des Logs auf. " );
                  console.error( error, jqXHR );
              }
          });
      }

      var formatTime = function(ts) {
        var date = new Date(ts);
        var d = date.getDate();
        var m = date.getMonth() + 1;
        var y = date.getFullYear();
        var time = date.toTimeString().substring(0, 8);
        return '' + y + '-' + (m<=9 ? '0' + m : m) + '-' + (d <= 9 ? '0' + d : d) + ' ' + time;
      };

      function setLog(data) {

        // fill div with data from content
        var content = "";
        for (var i=0; i < data.length; i++) {
          var row = data[i];
          if (row.value) {
            content += "<div class='" + row.classification.toLowerCase() + "'>" + formatTime(row.time) + " - [" + row.classification + "] " + row.value + "</div>";
          }
        }

        $("#statusContent").html( content );
      }

      function showDetailedImportLog() {
          dialog.dialog("open");
          $("#dialog-detailed .content").html( "wird geladen ..." );
          $.ajax( "../rest/status/${ instance.name }/import_log", {
              type: "GET",
              contentType: 'application/json',
              success: function(data) {
                  $("#dialog-detailed .content").html( data.replace(/\n/g, "<br>").replace(/<br>  */g,"<br>&nbsp&nbsp&nbsp&nbsp;") );
              }
          });

      }
    </script>

  </head>
  <body>

    <c:set var="activeTab" value="8" scope="request"/>
    <c:import url="includes/body.jsp"></c:import>

  </body>
</html>
