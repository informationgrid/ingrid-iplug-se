<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Instance</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

<link rel="stylesheet" type="text/css" href="css/yui/build/menu/assets/skins/sam/menu.css" />
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="css/yui/build/menu/menu-min.js"></script>

</head>
<body  class="yui-skin-sam">
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
						<script type="text/javascript">
						    YAHOO.util.Event.onContentReady("leftNav", function () {
						        var oMenu = new YAHOO.widget.Menu("leftNav", { 
						                                                position: "static", 
						                                                hidedelay:  750, 
						                                                lazyload: true });
						        oMenu.render();            
						    });
						</script>					
						
						<div id="leftNav" class="yuimenu">
					    <div class="bd">
					        <h6>General Instances</h6>
					        <ul>
								<c:forEach items="${rootContexts}" var="rootContext">
									<li class="yuimenuitem"><a class="yuimenuitemlabel" href="${rootContext}">${rootContext}</a></li>
								</c:forEach>
					        </ul>
					        <h6 class="first-of-type">Instances</h6>
					        <ul class="first-of-type">
								<c:forEach items="${navigations}" var="navigation">
									<li class="yuimenuitem"><a class="yuimenuitemlabel" href="${navigation}">${navigation}</a></li>
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