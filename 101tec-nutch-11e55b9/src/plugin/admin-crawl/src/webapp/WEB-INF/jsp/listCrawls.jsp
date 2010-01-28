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
	<title><fmt:message key="listCrawls.title" bundle="${localBundle}"/></title>
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
	<div id="doc2">
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
					
					<div style="float:right">
						<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/add.png" align="absmiddle"/> <b><a href="#" id="showCreateCrawl"><fmt:message key="listCrawls.createCrawl" bundle="${localBundle}"/></a></b>
					</div>
					<h3><fmt:message key="listCrawls.headline" bundle="${localBundle}"/></h3>
					<div id="markup">
					    <table id="crawls">
					        <thead>
					            <tr>
					            	<th><fmt:message key="listCrawls.searchable" bundle="${localBundle}"/></th>
					            	<th><fmt:message key="listCrawls.status" bundle="${localBundle}"/></th>
					            	<th><fmt:message key="listCrawls.path" bundle="${localBundle}"/></th>
					                <th><fmt:message key="listCrawls.size" bundle="${localBundle}"/></th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${crawlPaths}" var="crawlPath" varStatus="i">
						            <tr>
						            	<td>
						            		<div class="<c:choose><c:when test="${crawlPath.searchable}">switchOff</c:when><c:otherwise>switchOn</c:otherwise></c:choose>" id="switch_${i.index}">
						            			<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/switch_button.png" id="button_${i.index}"/>
						            		</div>
						            		
						            		
						            		<script>
											function init_${i.index}(){
												var myAnimOn_${i.index} = new YAHOO.util.Motion('button_${i.index}', {points: { by: [29, 0] } }, 0.5, YAHOO.util.Easing.easeOut);
												var myAnimOff_${i.index} = new YAHOO.util.Motion('button_${i.index}', {points: { by: [-29, 0] } }, 0.5, YAHOO.util.Easing.easeOut);

												 var switchOn_${i.index} = function(){
													 var myAnimOn_${i.index} = new YAHOO.util.Motion('button_${i.index}', {points: { by: [29, 0] } }, 0.5, YAHOO.util.Easing.easeOut);
													 YAHOO.util.Event.removeListener("switch_${i.index}", "mousedown");	
													 YAHOO.util.Event.addListener("switch_${i.index}", "mousedown", function(){ 
														 myAnimOn_${i.index}.onComplete.subscribe(switchOff_${i.index}); 				
														 myAnimOn_${i.index}.animate();
													 });
													  document.getElementById('addToSearch_${i.index}').submit();
													 }		
												 var switchOff_${i.index} = function(){
													 var myAnimOff_${i.index} = new YAHOO.util.Motion('button_${i.index}', {points: { by: [-29, 0] } }, 0.5, YAHOO.util.Easing.easeOut);
													 YAHOO.util.Event.removeListener("switch_${i.index}", "mousedown");	
													 YAHOO.util.Event.addListener("switch_${i.index}", "mousedown", function(){ 
														 myAnimOff_${i.index}.onComplete.subscribe(switchOn_${i.index}); 
														 myAnimOff_${i.index}.animate();
													 });
													  document.getElementById('removeFromSearch_${i.index}').submit();
													 }
												
												 YAHOO.util.Event.addListener("switch_${i.index}", "mousedown", function(){ 
													 <c:choose>
													 	<c:when test="${crawlPath.searchable}">
														 	myAnimOff_${i.index}.onComplete.subscribe(switchOff_${i.index}); 
														 	myAnimOff_${i.index}.animate();
													 	</c:when>
													 	<c:when test="${!crawlPath.searchable}">
														 	myAnimOn_${i.index}.onComplete.subscribe(switchOn_${i.index}); 
														 	myAnimOn_${i.index}.animate();
													 	</c:when>
													 </c:choose>	
													 }
												 );
											}
											YAHOO.util.Event.onAvailable("switch_${i.index}", init_${i.index});
											</script>
						            		
						            		
						            		<form id="addToSearch_${i.index}" action="addToSearch.html" method="post">
						            			<input type="hidden" name="crawlFolder" value="${crawlPath.path.name}"/>
						            		</form>
						            		<form id="removeFromSearch_${i.index}" action="removeFromSearch.html" method="post">
						            			<input type="hidden" name="crawlFolder" value="${crawlPath.path.name}"/>
						            		</form>
						            	</td>
						            	<td>
						            		<c:choose>
						            			<c:when test="${empty runningCrawl}">
								            		<a href="#" id="showStartCrawl${i.index}" onclick="document.getElementById('crawlFolder').value = '${crawlPath.path.name}'"><img src="<%=request.getContextPath()%>/theme/${theme}/gfx/play.png"/></a>
						            			</c:when>
						            			<c:otherwise>
						            				<c:choose>
						            					<c:when test="${crawlPath.running}">
										            		<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/loading.gif"/>
						            					</c:when>
						            					<c:otherwise>
										            		<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/play_inactive.png"/>
						            					</c:otherwise>
						            				</c:choose>
						            			</c:otherwise>
						            		</c:choose>
						            	</td>
						            	<td><a href="crawlDetails.html?crawlFolder=${crawlPath.path.name}">${crawlPath.path.name}</a></td>
						                <td>${crawlPath.size}</td>
						            </tr>
								</c:forEach>
					        </tbody>
					    </table>
					</div>
					<script type="text/javascript">
					 function renderTable() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
								{key:"searchable",label:"<fmt:message key="listCrawls.searchable" bundle="${localBundle}"/>", sortable:true},
								{key:"status",label:"<fmt:message key="listCrawls.status" bundle="${localBundle}"/>", sortable:true},
								{key:"path",label:"<fmt:message key="listCrawls.path" bundle="${localBundle}"/>", sortable:true},
					            {key:"size",label:"<fmt:message key="listCrawls.size" bundle="${localBundle}"/>", sortable:true},
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("crawls"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"searchable"},{key:"status"}, {key:"path"},{key:"size", parser:"number"},
					            ]
					        };
					
					        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource,{sortedBy:{key:"path",dir:"desc"}});
					        
					        return {
					            oDS: myDataSource,
					            oDT: myDataTable
					        };
					    }();
					};
					</script>						
		
				<script>
					YAHOO.namespace("example.container");
					function initCreateCrawl() {
						var handleYes = function() {
						    this.form.submit();
						};
						
						var handleNo = function() {
						    this.hide();
						};
					
						YAHOO.example.container.createCrawl = 
						    new YAHOO.widget.SimpleDialog("createCrawl", 
						             { width: "300px",
						               fixedcenter: true,
						               visible: false,
						               draggable: false,
						               close: true,
						               text: "<fmt:message key="listCrawls.wantContinue" bundle="${localBundle}"/>",
						               icon: YAHOO.widget.SimpleDialog.ICON_HELP,
						               constraintoviewport: true,
						               buttons: [ { text:"<fmt:message key="button.yes" bundle="${globalBundle}"/>", handler:handleYes, isDefault:true },
						                          { text:"<fmt:message key="button.no" bundle="${globalBundle}"/>",  handler:handleNo } ]
						             } );
						YAHOO.example.container.createCrawl.setHeader("<fmt:message key="listCrawls.areYouSure" bundle="${localBundle}"/>");
						YAHOO.example.container.createCrawl.render();
						YAHOO.util.Event.addListener("showCreateCrawl", "click", YAHOO.example.container.createCrawl.show, YAHOO.example.container.createCrawl, true);

						YAHOO.example.container.startCrawl = 
						    new YAHOO.widget.Dialog("startCrawl", 
						             { width: "500px",
						               fixedcenter: true,
						               visible: ${showDialog},
						               draggable: false,
						               close: true,
						               constraintoviewport: true,
						               buttons: [ { text:"<fmt:message key="button.start" bundle="${globalBundle}"/>", handler:handleYes, isDefault:true },
						                          { text:"<fmt:message key="button.cancel" bundle="${globalBundle}"/>",  handler:handleNo } ]
						             } );
						YAHOO.example.container.startCrawl.render();
						<c:forEach items="${crawlPaths}" var="crawlPath" varStatus="i">
						YAHOO.util.Event.addListener("showStartCrawl${i.index}", "click", YAHOO.example.container.startCrawl.show, YAHOO.example.container.startCrawl, true);
						</c:forEach>
						
					}
					YAHOO.util.Event.onDOMReady(renderTable);              
					YAHOO.util.Event.onDOMReady(initCreateCrawl);
				</script>
					
				<div id="createCrawl">
					<form:form method="post" action="createCrawl.html">
					</form:form>
				</div>
				<div id="startCrawl">
					<div class="hd"></div>
					<div class="bd">
					<form:form method="post" action="startCrawl.html" modelAttribute="crawlCommand">
						
						<fieldset>
							<legend><fmt:message key="listCrawls.startCrawl" bundle="${localBundle}"/></legend>
							<row>
								<label><fmt:message key="listCrawls.crawlName" bundle="${localBundle}"/>:</label>
								<field><input type="text" name="crawlFolder" id="crawlFolder" value="" readonly="readonly"/></field>
								<desc></desc>
							</row>
							<row>
								<label><fmt:message key="listCrawls.crawlDepth" bundle="${localBundle}"/>:</label>
								<field>
						           <form:select path="depth" items="${depths}"/>
						            <div class="error"><form:errors path="depth" /></div>
						        </field>
						        <desc></desc>
					        </row>
						    <row>
						        <label><fmt:message key="listCrawls.pagesPerSegment" bundle="${localBundle}"/>:</label>
						        <field>
						         	<form:input path="topn"/>
						            <div class="error"><form:errors path="topn" /></div>
						        </field>
						        <desc></desc>
						    </row>
						    <row>
						        <field>
						            <div class="error"><form:errors path="globalRejectAttribute" /></div>
						        </field>
						        <desc></desc>
						    </row>
						    
						</fieldset>
					</form:form>
					</div>
				</div>
				
				
				
			</div>	
		</div>
	</div>
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>

</body>
</html>
