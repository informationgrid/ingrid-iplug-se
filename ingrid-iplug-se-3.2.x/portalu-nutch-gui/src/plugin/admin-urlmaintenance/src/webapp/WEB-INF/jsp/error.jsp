<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="includes/include.jsp" %>
<html>
<head>
    <title>PortalU URL-Pflege - Willkommen</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
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
					<h3>Willkommen ${partnerProviderCommand.provider}</h3>
					
					<div id="demo" class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li><a href="listWebUrls.html"><em>Web Url's</em></a></li>
					    </ul>            
					</div>						

				    <div>
				        <div>
				        	<p>&nbsp;</p>
				        	<p><pre>${message}</pre></p>
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