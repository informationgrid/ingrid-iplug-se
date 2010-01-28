<%--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title><fmt:message key="system.title" bundle="${localBundle}"/></title>

	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo/yahoo-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/event/event-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/getLog.js"></script>
	 
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
	<script>
		var lineCount = 1000;
	</script>
</head>

<body class="yui-skin-sam" onload="getLog(lineCount)">
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
						<li class="selected"><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
					</c:when>
					<c:otherwise>
						<li><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
	    </ul>
	</div>
	
	<div id="bd"> 
		<div id="yui-main"> 
			<div class="yui-b">
				<h3><fmt:message key="system.headline" bundle="${localBundle}"/></h3>
				<div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label><fmt:message key="system.ramUsed" bundle="${localBundle}"/>:</label> ${systemInfo.usedMemory} MB
					</div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label><fmt:message key="system.ramAvailable" bundle="${localBundle}"/>:</label> ${systemInfo.maxMemory} MB
					</div>
					<div style="border-bottom:1px dotted #CCCCCC; padding:10px 0 10px 0">
						<label><fmt:message key="system.load" bundle="${localBundle}"/>:</label> ${systemInfo.usedMemoryInPercent}%
					</div>
				</div>	
				
				<div style="margin-top:25px"></div>
				<input type="hidden" id="mode" value="start"/>
				<h3><fmt:message key="system.logfile" bundle="${localBundle}"/></h3>
				<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/console.png" align="absmiddle"/> <fmt:message key="system.show" bundle="${localBundle}"/> <input type="text" id="lineCount" value="" size="5"/> <fmt:message key="system.recentLines" bundle="${localBundle}"/> <input type="button" value="<fmt:message key="button.set" bundle="${globalBundle}"/>" onClick="lineCount = document.getElementById('lineCount').value; getLog(lineCount); "/>
				<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/play_inactive.png" align="absmiddle" id="start" onclick="handleStartStop('start')" style="cursor:pointer">
				<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/pause.png" align="absmiddle" id="stop" onclick="handleStartStop('stop')"  style="cursor:pointer">
				<script>
					function handleStartStop(action){
						var startImage = document.getElementById('start');
						var stopImage = document.getElementById('stop');
						if(action == 'start'){
							startImage.src = '${theme}/gfx/play_inactive.png';
							stopImage.src = '${theme}/gfx/pause.png';
							if(document.getElementById('mode').value != 'start'){
								document.getElementById('mode').value = 'start';
								getLog(lineCount);
							}
						}else if(action == 'stop'){
							startImage.src = '${theme}/gfx/play.png';
							stopImage.src = '${theme}/gfx/pause_inactive.png';
							document.getElementById('mode').value = 'stop';
						}
					}
				</script>
				<script>
					document.getElementById('lineCount').value = lineCount;
				</script>
				<div id="logFileContainer" style="height:400px; overflow:auto; background:#F4F4F4; border:1px solid #CCCCCC; font-size:11px" onmousedown="document.getElementById('mode').value = 'stop'" onmouseup="document.getElementById('mode').value = 'start'; getLog(lineCount)"></div>
				
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
