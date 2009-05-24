<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>

<script src="css/yui/build/yahoo/yahoo-min.js"></script>
<script src="css/yui/build/event/event-min.js"></script>
<script src="css/yui/build/connection/connection-min.js"></script>

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						<form:form action="index.html" commandName="partnerProviderCommand" method="post">
							<fieldset>
								<label>Choose Partner Provider</label>
								<form:label path="partner">Partner</form:label>
								<form:select path="partner" items="${partners}" itemLabel="name" itemValue="name"/>
								<form:label path="provider">Provider</form:label>
								<form:select path="provider" items="${providers}" itemLabel="name" itemValue="name"/>
							</fieldset>
							<input type="submit" value="Next"/>
						</form:form>
					</div>
				</div>
				<div class="yui-b">
					<div id="leftNav" class="yuimenu">
					    <div class="bd">
					        <h6>General Components</h6>
					        <ul>
								<c:forEach items="${generalComponents}" var="component">
									<li class="yuimenuitem"><a class="yuimenuitemlabel" href="${component.link}">${component.name}</a></li>
								</c:forEach>
					        </ul>
					        <h6 class="first-of-type">Components</h6>
					        <ul class="first-of-type">
								<c:forEach items="${components}" var="component">
									<li class="yuimenuitem"><a class="yuimenuitemlabel" href="${component.link}">${component.name}</a></li>
								</c:forEach>
					        </ul>
					    </div>
					</div>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>