<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
      $(document).ready(function () {
        //$("#statusContainer").hide();
      });
    </script>

  </head>
  <body>

    <c:set var="activeTab" value="8" scope="request"/>
    <c:import url="includes/body.jsp"></c:import>

  </body>
</html>
