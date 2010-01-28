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
	<title><fmt:message key="urlupload.title" bundle="${localBundle}"/></title>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
	
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/event/event-min.js" ></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />

	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/container-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/animation/animation-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
	
	
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="/WEB-INF/jsp/includes/header.jsp" %>
		</div>
		
		<c:if test="${!empty instanceNavigation}">
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
		</c:if>
		
		<c:if test="${!empty componentNavigation}">
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
		</c:if>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3><fmt:message key="urlupload.headline" bundle="${localBundle}"/></h3>
					<div class="row">
						<label><fmt:message key="urlupload.bwList" bundle="${localBundle}"/>:</label> <c:choose><c:when test="${isBwEnabled}"><fmt:message key="urlupload.on" bundle="${localBundle}"/></c:when><c:otherwise><fmt:message key="urlupload.off" bundle="${localBundle}"/></c:otherwise></c:choose>
					</div>
					<div class="row">
						<label><fmt:message key="urlupload.metadata" bundle="${localBundle}"/>:</label> <c:choose><c:when test="${isMetadataEnabled}"><fmt:message key="urlupload.on" bundle="${localBundle}"/></c:when><c:otherwise><fmt:message key="urlupload.off" bundle="${localBundle}"/></c:otherwise></c:choose>
					</div>
					
					<br/>
					<h3><fmt:message key="urlupload.uploadUrls" bundle="${localBundle}"/></h3>
					<form action="upload.html" method="post" enctype="multipart/form-data">
					<fieldset>
					    <legend><fmt:message key="urlupload.uploadUrlsAsZip" bundle="${localBundle}"/></legend>
					    
					    <row>
					        <label><fmt:message key="urlupload.type" bundle="${localBundle}"/>:</label>
					        <field>
					           <select name="type">
									<option value="start"><fmt:message key="urlupload.startUrls" bundle="${localBundle}"/></option>
									<option value="limit" <c:if test="${!isBwEnabled}">disabled="disabled"</c:if>><fmt:message key="urlupload.limitUrls" bundle="${localBundle}"/></option>
									<option value="exclude" <c:if test="${!isBwEnabled}">disabled="disabled"</c:if>><fmt:message key="urlupload.excludeUrls" bundle="${localBundle}"/></option>
									<option value="metadata" <c:if test="${!isMetadataEnabled}">disabled="disabled"</c:if>><fmt:message key="urlupload.urlMetadata" bundle="${localBundle}"/></option>
								</select>
						     </field>
					        <desc></desc>
					    </row>
					    
					    <row>
					        <label><fmt:message key="urlupload.zipFile" bundle="${localBundle}"/>:</label>
					        <field>
					           <input name="file" type="file" value=""/>
						     </field>
					        <desc></desc>
					    </row>
					    
					     <row>
					        <label>&nbsp;</label>
					        <field>
					            <input type="submit" value="<fmt:message key="button.upload" bundle="${globalBundle}"/>"/>
					        </field>
					    </row>
					</fieldset>
					</form>
					
					
					<h3><fmt:message key="urlupload.availableFiles" bundle="${localBundle}"/></h3>
					<div class="row">
						<label><fmt:message key="urlupload.startUrlFiles" bundle="${localBundle}"/>:</label>
						&nbsp;<c:forEach items="${startUrls}" var="zip">${zip.name} <img src="<%=request.getContextPath()%>/theme/${theme}/gfx/delete.png" align="absmiddle" style="cursor:pointer" onclick="submitDelete('start','${zip.name}')" titel="<fmt:message key="button.delete" bundle="${globalBundle}"/>"/>, </c:forEach>
					</div>
					<div class="row">
						<label><fmt:message key="urlupload.limitUrlFiles" bundle="${localBundle}"/>:</label>&nbsp;
						<c:choose>
							<c:when test="${isBwEnabled}">
								<c:forEach items="${limitUrls}" var="zip">${zip.name} <img src="<%=request.getContextPath()%>/theme/${theme}/gfx/delete.png" align="absmiddle" style="cursor:pointer" onclick="submitDelete('limit','${zip.name}')" titel="<fmt:message key="button.delete" bundle="${globalBundle}"/>"/>, </c:forEach>
							</c:when>
							<c:otherwise>
								<fmt:message key="urlupload.na" bundle="${localBundle}"/>
							</c:otherwise>
						</c:choose>
					</div>
					<div class="row">
						<label><fmt:message key="urlupload.excludeUrlFiles" bundle="${localBundle}"/>:</label>&nbsp;
						<c:choose>
							<c:when test="${isBwEnabled}">
								<c:forEach items="${excludeUrls}" var="zip">${zip.name} <img src="<%=request.getContextPath()%>/theme/${theme}/gfx/delete.png" align="absmiddle" style="cursor:pointer" onclick="submitDelete('exclude','${zip.name}')" titel="<fmt:message key="button.delete" bundle="${globalBundle}"/>"/>, </c:forEach>
							</c:when>
							<c:otherwise>
								<fmt:message key="urlupload.na" bundle="${localBundle}"/>
							</c:otherwise>
						</c:choose>
					</div>
					<div class="row">
						<label><fmt:message key="urlupload.metadataFiles" bundle="${localBundle}"/>:</label>&nbsp;
						<c:choose>
							<c:when test="${isMetadataEnabled}">
								<c:forEach items="${metadataUrls}" var="zip">${zip.name} <img src="<%=request.getContextPath()%>/theme/${theme}/gfx/delete.png" align="absmiddle" style="cursor:pointer" onclick="submitDelete('metadata','${zip.name}')" titel="Löschen"/>, </c:forEach>
							</c:when>
							<c:otherwise>
								<fmt:message key="urlupload.na" bundle="${localBundle}"/>
							</c:otherwise>
						</c:choose>
					</div>
					
					<script>
						function submitDelete(type, fileName){
							document.getElementById('deleteType').value = type; 
							document.getElementById('deleteFile').value = fileName;
							document.getElementById('deleteForm').submit();
						}
					</script>
					
					<form action="deleteZip.html" method="POST" id="deleteForm">
						<input type="hidden" name="type" id="deleteType"/>
						<input type="hidden" name="file" id="deleteFile"/>
					</form>
					
				
				</div>	
		</div>
		<div class="yui-b">
			<h3><fmt:message key="urlupload.help" bundle="${localBundle}"/></h3>
			<fmt:message key="urlupload.helpText" bundle="${localBundle}"/>
			<!-- 
			<ul class="decorated">
				<li>Wo schaltet man Config An / Aus?</li>
				<li>Was machen Limit / Exclude / Metadaten</li>
				<li>Wie ist eine Datei aufgebaut? (1 Url pro Zeile, Metadaten tab-sep. key:value ...)</li>
			</ul>
			 -->
		</div> 
	</div>
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>

</body>
</html>
