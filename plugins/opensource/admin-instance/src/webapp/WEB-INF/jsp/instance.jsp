<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - Instanzen</title>

	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	 
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
</head>

<body class="yui-skin-sam">
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
				<form:form commandName="createInstance" action="index.html" method="post">
				<fieldset>
				    <legend>Neue Instanz anlegen</legend>
				    
				    <row>
				        <label><form:label path="folderName" >Name: </form:label></label>
				        <field>
				           <form:input path="folderName"/>
				            <div class="error"><form:errors path="folderName" /></div>
				        </field>
				        <desc></desc>
				    </row>
				    
				     <row>
				        <label>&nbsp;</label>
				        <field>
				            <input type="submit" value="Speichern"/>
				        </field>
				    </row>
				</fieldset>
				</form:form>
			</div> 
		</div> 
		<div class="yui-b">
			<h3>Hilfe</h3>
			Nach dem Anlegen einige Sekunden warten und diese Seite neu laden. Die Instanz erscheint als Tab in der Hauptnavigation.
		</div> 
	</div>
	
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
