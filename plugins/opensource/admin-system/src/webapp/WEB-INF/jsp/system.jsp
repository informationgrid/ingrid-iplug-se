<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - System</title>

	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo/yahoo-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/event/event-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="${theme}/js/getLog.js"></script>
	 
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
	<script>
		var lineCount = 1000;
	</script>
</head>

<body class="yui-skin-sam" onLoad="getLog(1000);">
<div id="doc2" class="yui-t4">					
	<div id="hd">
		<%@ include file="/WEB-INF/jsp/includes/header.jsp" %>
	</div>
	
	<div class="yui-navset nav">
	    <ul class="yui-nav">
			<c:forEach items="${instanceNavigation}" var="navigation">
				<c:choose>
					<c:when test="${navigation.name == selectedInstance}">
						<li class="selected"><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
					</c:when>
					<c:otherwise>
						<li><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
	    </ul>
	</div>
	
	<div id="subnav">
	    <ul>
			<c:forEach items="${componentNavigation}" var="navigation">
				<c:choose>
					<c:when test="${navigation.name == selectedComponent}">
						<li class="selected"><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
					</c:when>
					<c:otherwise>
						<li><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
	    </ul>
	</div>
	
	<div id="bd"> 
		<div id="yui-main"> 
			<div class="yui-b">
				<h3>System Übersicht</h3>
				<div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label>RAM (benutzt):</label> ${systemInfo.usedMemory} MB
					</div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label>RAM (verfügbar):</label> ${systemInfo.maxMemory} MB
					</div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label>Auslastung:</label> ${systemInfo.usedMemoryInPercent}%
					</div>
				</div>	
				
				<div style="margin-top:25px"></div>
				<h3>Log Datei</h3>
				<img src="${theme}/gfx/console.png" align="absmiddle"/> Zeige <input type="text" id="lineCount" value="" size="5"/> letzte Zeilen <input type="button" value="Setzen" onClick="lineCount = document.getElementById('lineCount').value; getLog(lineCount); "/>
				<script>
					document.getElementById('lineCount').value = lineCount;
				</script>
				<div id="logFileContainer" style="height:400px; overflow:auto; background:#F4F4F4; border:1px solid #CCCCCC; font-size:11px"></div>
				
			</div> 
		</div> 
		<div class="yui-b">
			
		</div> 
	</div>
	
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
