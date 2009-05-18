<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="css/yui/build/tabview/assets/skins/sam/tabview.css">
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
<script type="text/javascript" src="css/yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="css/yui/build/tabview/tabview-min.js"></script>



<link rel="stylesheet" type="text/css" href="css/yui/build/paginator/assets/skins/sam/paginator.css" />
<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
<script type="text/javascript" src="css/yui/build/json/json-min.js"></script>
<script type="text/javascript" src="css/yui/build/paginator/paginator-min.js"></script>
<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>

<script type="text/javascript" src="css/yui/build/yahoo/yahoo-min.js" ></script>
<script type="text/javascript" src="css/yui/build/event/event-min.js" ></script>

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						Welcome -${partnerProviderCommand.provider}-
						<form:form action="createStartUrl.html" method="post" modelAttribute="startUrlCommand">
							<fieldset>
								<legend>Edit Start Url</legend>
								<form:label path="url">Url</form:label>
								<form:input path="url"/>
							</fieldset>
							<input type="submit" value="Weiter"/>
						</form:form>
					</div>
				</div>
				<div class="yui-b">
				Welcome Navigation
					<c:forEach items="${navigations}" var="navigation">
						<a href="${navigation}">${navigation}</a>
					</c:forEach>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>