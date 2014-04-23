<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
	<title>PortalU URL-Pflege - Start URLs</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
</head>

<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="../includes/header.jsp" %>
		</div>
		
		<%@ include file="../includes/menu.jsp" %>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3>Web URLs (${partnerProviderCommand.partner.name} / ${partnerProviderCommand.provider.name})</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="../catalog/listTopicUrls.html"><em>Katalog-URLs</em></a></li>
					        <li class="selected"><a href="../web/listWebUrls.html"><em>Web-URLs</em></a></li>
					        <li><a href="../import/importer.html"><em>Importer</em></a></li>
					    </ul>            
					</div>
					
					<form:form action="createStartUrl.html" method="post" modelAttribute="startUrlCommand">
						<fieldset>
							<c:choose>
								<c:when test="${startUrlCommand.id > -1}">
									<legend>Web-URL bearbeiten - Start-URL</legend>
								</c:when>
								<c:otherwise>
									<legend>Web-URL anlegen - Start-URL</legend>										
								</c:otherwise>
							</c:choose>
							<row>
						        <label>Start-URL:</label>
						        <field>
						           <form:input path="url"/>
						            <div class="error"><form:errors path="url" /></div>
						        </field>
						        <desc></desc>
						    </row>
						    
						    <row>
						        <label>&nbsp;</label>
						        <field>
						            <input type="submit" value="Weiter"/>
						        </field>
						    </row>
							
						</fieldset>
					</form:form>
				</div>
			</div>
			<div class="yui-b">
			
			</div>
		</div>
		
		<div id="ft">
			<%@ include file="../includes/footer.jsp" %>
		</div>
		
	</div>
</body>
</html>