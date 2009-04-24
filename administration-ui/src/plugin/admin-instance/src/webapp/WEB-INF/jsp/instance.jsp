<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Instance</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
</head>
<body>
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						<form:form commandName="createInstance" action="index.html" method="post">
							<form:errors path="folderName" />
							<form:label path="folderName" >Name: </form:label>
							<form:input path="folderName"/>
							<input type="submit" value="Create" />
						</form:form>
						
						<c:forEach items="${instances}" var="instance">
							<a href="../${instance}">${instance}</a>
						</c:forEach>
						
					</div>
				</div>
				<div class="yui-b">
					Instance Navigation
					<c:forEach items="${navigations}" var="navigation">
						<a href="${navigation}">${navigation}</a>
					</c:forEach>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>