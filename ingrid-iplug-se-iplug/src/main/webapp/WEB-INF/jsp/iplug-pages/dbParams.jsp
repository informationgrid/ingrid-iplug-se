<%@ include file="/WEB-INF/jsp/base/include.jsp" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="de.ingrid.admin.security.IngridPrincipal"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />

</head>
<body>
    <div id="header">
        <img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
        <h1><fmt:message key="DatabaseConfig.main.configuration"/></h1>
        <%
          java.security.Principal  principal = request.getUserPrincipal();
          if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
        %>
            <div id="language"><a href="../base/auth/logout.html"><fmt:message key="DatabaseConfig.main.logout"/></a></div>
        <%
          }
        %>
    </div>
    <div id="help"><a href="#">[?]</a></div>

    <c:set var="active" value="dbParams" scope="request"/>
    <c:import url="../base/subNavi.jsp"></c:import>

    <div id="contentBox" class="contentMiddle">
        <h1 id="head">Database Parameter</h1>
        <div class="controls">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('dbConfig').submit();">Weiter</a>
        </div>
        <div class="controls cBottom">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('dbConfig').submit();">Weiter</a>
        </div>
        <div id="content">
            <form:form method="post" action="dbParams.html" modelAttribute="dbConfig">
                <input type="hidden" name="action" value="submit" />
                <input type="hidden" name="id" value="" />
                <table id="konfigForm">
                    <br />
                    <tr>
                        <td colspan="2"><h3>Choose the database config parameters:</h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">database driver</td>
                        <td>
                            <form:input path="dataBaseDriver" />
                            <form:errors path="dataBaseDriver" cssClass="error" element="div" />
                            <br />
                            Please supply the database driver. Valid entries are:
                            <ul>
                                <li>MySql: com.mysql.jdbc.Driver</li>
                                <li>Oracle: oracle.jdbc.driver.OracleDriver</li>
                            </ul>
                            If the database is not listed here, the database driver must be present in the lib directory of the iplug.
                            <p style="color: gray;">(Sample: com.mysql.jdbc.Driver)</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">connection url</td>
                        <td>
                            <form:input path="connectionURL" />
                            <form:errors path="connectionURL" cssClass="error" element="div" />
                            <br />
                            Please supply the jdbc connection url.
                            <p style="color: gray;">(Sample: jdbc:mysql://localhost:3306/igc)</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">user</td>
                        <td>
                            <form:input path="user" />
                            <form:errors path="user" cssClass="error" element="div" />
                            <br />
                            Please supply the database user .
                            <p style="color: gray;">(Sample: igc)</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">password</td>
                        <td>
                            <form:input path="password" />
                            <form:errors path="password" cssClass="error" element="div" />
                            <br />
                            Please supply the password for the database user.
                            <p style="color: gray;">(Sample: 5&hftre)</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol">database schema</td>
                        <td>
                            <form:input path="schema" />
                            <form:errors path="schema" cssClass="error" element="div" />
                            <br />
                            Optional: Supply database schema if needed.
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>

