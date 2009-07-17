<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - Crawls</title>
	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="${theme}/js/yui/build/event/event-min.js" ></script>
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/button/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/container/assets/skins/sam/container.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/container/container-min.js"></script>
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2">
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
					<c:forEach items="${statistics}" var="statistic">
						${statistic.host}
						${statistic.crawldbCount}
						${statistic.segmentCount}
					</c:forEach>	
				</div>	
		</div>
	</div>
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
