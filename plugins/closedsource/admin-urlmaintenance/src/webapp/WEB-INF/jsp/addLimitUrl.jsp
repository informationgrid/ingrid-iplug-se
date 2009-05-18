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
						<c:set var="maxLimitUrls" value="${fn:length(startUrlCommand.limitUrlCommands)}"/>
						<c:set var="limitUrlCounter" value="-1"/>
						<c:forEach items="${startUrlCommand.limitUrlCommands}" var="limitUrl">
							<c:set var="limitUrlCounter" value="${limitUrlCounter+1}"/>
							<c:choose>
								<c:when test="${limitUrlCounter < maxLimitUrls-1}">
									<form:form action="removeLimitUrl.html" method="post" modelAttribute="startUrlCommand">
										<form:label path="limitUrlCommands[${limitUrlCounter}].url">${startUrlCommand.limitUrlCommands[limitUrlCounter].url}</form:label>
										<input type="hidden" name="index" value="${limitUrlCounter}" />
										<input type="submit" value="Delete"/>
									</form:form>
								</c:when>
								<c:otherwise>
									<form:form action="addLimitUrl.html" method="post" modelAttribute="startUrlCommand">
										<fieldset>
											<legend>Url</legend>
											<form:input path="limitUrlCommands[${limitUrlCounter}].url"/>
										</fieldset>
										<fieldset>
											<legend>Sprache</legend>
											<form:select path="limitUrlCommands[${limitUrlCounter}].metadatas">
												<form:options itemLabel="metadataValue" itemValue="id" items="${langs}"/>
											</form:select>
										</fieldset>
										<fieldset>
											<legend>Datatypes</legend>
											<form:checkboxes path="limitUrlCommands[${limitUrlCounter}].metadatas" items="${datatypes}" itemLabel="metadataValue" itemValue="id"/>
										</fieldset>
										<input type="submit" value="Add"/>
									</form:form>
								</c:otherwise>
							</c:choose>
						</c:forEach>
						<a href="addExcludeUrl.html" >Weiter</a>
						
						
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