<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="de.ingrid.admin.security.IngridPrincipal"%><html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title>Portal U Administration</title>
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
        <h1>Konfiguration</h1>
		<%
		java.security.Principal  principal = request.getUserPrincipal();
		if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
		%>
			<div id="language"><a href="<%=request.getContextPath()%>/base/auth/logout.html">Logout</a></div>
		<%
		}
		%>
    </div>
    
    <div id="help"><a href="#">[?]</a></div>
    
    <c:set var="active" value="heartbeat" scope="request"/>
    <c:import url="subNavi.jsp"></c:import>
    
    <div id="contentBox" class="contentMiddle">
        <h1 id="head">HeartBeat Setup</h1>
        <div id="content">
	        <br />
            <p>Hier sehen sie den Status der HeartBeats zu den IBus(sen).</p>
            
            <span><b>Status:</b>
                <c:choose>
                    <c:when test="${accurate}">
                        <span class="success">sendet</span>
                    </c:when>
                    <c:otherwise>
                        <span class="error">
			                <c:choose>
			                    <c:when test="${enabled}">sendet fehlerhaft</c:when>
			                    <c:otherwise>sendet nicht</c:otherwise>
			                </c:choose>
                        </span>
                    </c:otherwise>
                </c:choose>
            </span><br />
            <br />
            <c:choose>
                <c:when test="${enabled}">
                    <form action="" method="post">
                        <input type="hidden" name="action" value="stop" />
                        <input type="submit" name="submit" value="Verbindung trennen" />
                    </form><br />
                    <form action="" method="post">
                        <input type="hidden" name="action" value="restart" />
                        <input type="submit" name="submit" value="Verbindung neu starten" />
                    </form>
                 </c:when>
                 <c:otherwise>
                    <form action="" method="post">
                        <input type="hidden" name="action" value="start" />
                        <input type="submit" name="submit" value="Verbindung starten" />
                    </form>
                 </c:otherwise>
             </c:choose>
             <br />
             <p>Sie haben die Möglichkeit die HeartBeats zu stoppen, starten und neu zu starten.
             <br />Üblicherweiße sollte hier "<span class="success">sendet</span>" zu sehen sein.<br />
             Ist dies nicht der Falll, sondern erscheint hier "<span class="error">sendet nicht</span>" oder "<span class="error">sendet fehlerhaft</span>", kann sich das IPlug an mindestens einen IBus nicht anmelden oder es treten Fehler während des Sendens auf.</p>
        
        </div>
    </div>
    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>