<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="includes/include.jsp" %>
<html>
<head>
    <title>PortalU URL-Pflege - Willkommen</title>
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=rootPath%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=rootPath%>/theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2">					
		<div id="hd">
			<%@ include file="includes/header.jsp" %>
		</div>
		
		<%@ include file="includes/menu.jsp" %>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3>Willkommen! (${partnerProviderCommand.partner.name} / ${partnerProviderCommand.provider.name})</h3>
					
					<div id="demo" class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="./catalog/listTopicUrls.html"><em>Katalog-URLs</em></a></li>
					        <li><a href="./web/listWebUrls.html"><em>Web-URLs</em></a></li>
					        <li><a href="./import/importer.html"><em>Importer</em></a></li>
					    </ul>            
					</div>						

				    <div>
				        <div>
				        	<p>&nbsp;</p>
				        	<p>Willkommen in der Urlpflege. M&ouml;chten Sie Webseiten zur <i>PortalU&reg;-Suche</i> hinzuf&uuml;gen,
                             bearbeiten oder l&ouml;schen? Dann klicken Sie bitte auf den Reiter <b>Web-URLs</b>. Wenn Sie
                             hingegen Webseiten zu den PortalU&reg;-Rubriken <i>Umweltthemen, Messwerte</i> oder <i>Service</i> 
                             hinzuf&uuml;gen, bearbeiten oder l&ouml;schen m&ouml;chten, klicken Sie bitte auf den Reiter 
                             <b>Katalog-URLs</b>.</p>
				        </div>
				    </div>

				</div>
			</div>
			
		</div>
		<div id="ft">
			<%@ include file="includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>