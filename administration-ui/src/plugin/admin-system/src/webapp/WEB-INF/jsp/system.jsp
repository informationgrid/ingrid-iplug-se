<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
	<title>Welcome</title>

	<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

	<script type="text/javascript" src="css/yui/build/yahoo/yahoo-min.js"></script>
	<script type="text/javascript" src="css/yui/build/event/event-min.js"></script>
	<script type="text/javascript" src="css/yui/build/connection/connection-min.js"></script>
	
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
			Used Memory: ${systemInfo.usedMemory}
			Max. Memory: ${systemInfo.maxMemory}
			Used Memory in Percent: ${systemInfo.usedMemoryInPercent}
			
			<pre>
				<div id="logFileContainer" style="height:400px; overflow:auto; background:#F4F4F4; border:1px solid #CCCCCC; font-size:10px"></div>
			</pre>
			
			<script>
			//TODO recall this method only on success response 
				setInterval("makeRequest()", 5000);
			
				var div = document.getElementById('logFileContainer');
				
				var handleSuccess = function(o){
					if(o.responseText !== undefined){
						div.innerHTML = "<li>Server response: " + div.innerHTML + o.responseText + "</li>";
					}
				}
				
				var handleFailure = function(o){
					if(o.responseText !== undefined){
						div.innerHTML = "<li>Status code message: " + o.statusText + "</li></ul>";
					}
				}
				
				var callback =
				{
				  success:handleSuccess,
				  failure:handleFailure,
				};
				
				var sUrl = "log.html?file=hadoop.log&lines=30";
				
				function makeRequest(){
					var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
				}
			</script>
		</p>
	</div>
	<div id="ft">
		<p>Footer</p>
	</div>
</div>
</body>
</html>
