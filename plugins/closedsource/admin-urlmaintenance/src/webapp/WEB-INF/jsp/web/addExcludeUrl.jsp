<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Exclude URL</title>
	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="/WEB-INF/jsp/includes/header.jsp" %>
		</div>
		
		<c:if test="${!empty instanceNavigation}">
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
		</c:if>
		
		<c:if test="${!empty componentNavigation}">
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
		</c:if>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3>Web URLs</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li class="selected"><a href="listWebUrls.html"><em>Web Url's</em></a></li>
					    </ul>            
					</div>
					
					<c:set var="maxExcludeUrls" value="${fn:length(startUrlCommand.excludeUrlCommands)}"/>
					<c:set var="excludeUrlCounter" value="-1"/>
					<c:forEach items="${startUrlCommand.excludeUrlCommands}" var="excludeUrl">
						<c:set var="excludeUrlCounter" value="${excludeUrlCounter+1}"/>
						<c:choose>
							<c:when test="${excludeUrlCounter < maxExcludeUrls-1}">
								<div class="row">
								<form:form action="removeExcludeUrl.html" method="post" modelAttribute="startUrlCommand">
									${startUrlCommand.excludeUrlCommands[excludeUrlCounter].url}
									<input type="hidden" name="index" value="${excludeUrlCounter}" />
									<input type="image" src="${theme}/gfx/delete.png" align="absmiddle" title="Löschen"/>
								</form:form>
								</div>
							</c:when>
							<c:otherwise>
								<form:form action="addExcludeUrl.html" method="post" modelAttribute="startUrlCommand">
									<fieldset>
										<legend>Web URL anlegen</legend>
										<row>
									        <label>Exclude URL:</label>
									        <field>
									          <form:input path="excludeUrlCommands[${excludeUrlCounter}].url"/>
									            <div class="error"><form:errors path="excludeUrlCommands[${excludeUrlCounter}].url" /></div>
									        </field>
									        <desc></desc>
									    </row>
									    
									    <row>
									        <label>&nbsp;</label>
									        <field>
									            <input type="submit" value="Hinzufügen"/>
									            <input type="button" value="Weiter" onclick="window.location.href='finishWebUrl.html'" />
									        </field>
									    </row>
									</fieldset>    
									
								</form:form>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					
					
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