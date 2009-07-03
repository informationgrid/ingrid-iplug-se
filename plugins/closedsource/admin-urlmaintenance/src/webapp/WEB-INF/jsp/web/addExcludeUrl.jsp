<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						Welcome -${partnerProviderCommand.provider}-
						<c:set var="maxExcludeUrls" value="${fn:length(startUrlCommand.excludeUrlCommands)}"/>
						<c:set var="excludeUrlCounter" value="-1"/>
						<c:forEach items="${startUrlCommand.excludeUrlCommands}" var="excludeUrl">
							<c:set var="excludeUrlCounter" value="${excludeUrlCounter+1}"/>
							<c:choose>
								<c:when test="${excludeUrlCounter < maxExcludeUrls-1}">
									<form:form action="removeExcludeUrl.html" method="post" modelAttribute="startUrlCommand">
										<form:label path="excludeUrlCommands[${excludeUrlCounter}].url">${startUrlCommand.excludeUrlCommands[excludeUrlCounter].url}</form:label>
										<input type="hidden" name="index" value="${excludeUrlCounter}" />
										<input type="submit" value="Delete"/>
									</form:form>
								</c:when>
								<c:otherwise>
									<form:form action="addExcludeUrl.html" method="post" modelAttribute="startUrlCommand">
										<fieldset>
											<legend>Url</legend>
											<form:input path="excludeUrlCommands[${excludeUrlCounter}].url"/>
										</fieldset>
										<input type="submit" value="Add"/>
									</form:form>
								</c:otherwise>
							</c:choose>
						</c:forEach>
						<a href="finishWebUrl.html" >Weiter</a>
						
						
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