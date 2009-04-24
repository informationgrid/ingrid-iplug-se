<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

<script type="text/javascript" src="css/yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="css/yui/build/event/event-min.js"></script>
<script type="text/javascript" src="css/yui/build/connection/connection-min.js"></script>


</head>
<body>
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						Used Memory: ${systemInfo.usedMemory}
						Max. Memory: ${systemInfo.maxMemory}
						Used Memory in Percent: ${systemInfo.usedMemoryInPercent}
						
						<pre>
							<div id="logFileContainer"></div>
						</pre>
						
						<script>
						//TODO recall this method only on success response 
							setInterval("makeRequest()", 5000);
						
							var div = document.getElementById('logFileContainer');
							
							var handleSuccess = function(o){
								if(o.responseText !== undefined){
									div.innerHTML = "<li>Server response: " + o.responseText + "</li>";
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