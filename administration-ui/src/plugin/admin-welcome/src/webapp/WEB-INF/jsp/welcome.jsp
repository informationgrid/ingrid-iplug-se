<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
	<title>Welcome</title>

	<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	
	<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="css/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="css/yui/build/tabview/tabview-min.js"></script>
	 
</head>

<body class="yui-skin-sam">
<div id="doc">					
	<div id="hd">
		<p>Header</p>
	</div>
	<div id="bd">
		<p>
			<div id="instanceNavigation" class="yui-navset">
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
			<div id="componentNavigation" class="yui-navset">
			    <ul class="yui-nav">
					<c:forEach items="${componentNavigation}" var="navigation">
						<li><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
					</c:forEach>
			    </ul>
			</div>
		
		</p>
	</div>
	<div id="ft">
		<p>Footer</p>
	</div>
</div>
</body>
</html>
