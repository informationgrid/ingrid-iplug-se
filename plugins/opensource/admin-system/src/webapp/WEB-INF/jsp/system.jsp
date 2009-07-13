<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - System</title>

	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo/yahoo-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/event/event-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	 
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
</head>

<body class="yui-skin-sam">
<div id="doc2" class="yui-t4">					
	<div id="hd">
		<%@ include file="/WEB-INF/jsp/includes/header.jsp" %>
	</div>
	
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
	
	<div id="bd"> 
		<div id="yui-main"> 
			<div class="yui-b">
				<div>
					<label>RAM (benutzt):</label> ${systemInfo.usedMemory} MB<br/>
					<label>RAM (verfügbar):</label> ${systemInfo.maxMemory} MB<br/>
					<label>Auslastung:</label> ${systemInfo.usedMemoryInPercent}%<br/><br/>
				</div>	
			
				<h3>Log Datei</h3>
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
			</div> 
		</div> 
		<div class="yui-b">
			
		</div> 
	</div>
	
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
