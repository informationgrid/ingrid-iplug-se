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
	<title><fmt:message key="configuration.title" bundle="${localBundle}"/></title>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
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
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
	<style type="text/css">
		.yui-dt-liner {font-size:11px}
	</style>
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
				<h3><fmt:message key="configuration.headline" bundle="${localBundle}"/></h3>
				<p>
					<fmt:message key="configuration.byline" bundle="${localBundle}"/>
				</p>
				<div id="markup">
				    <table id="configurations">
				        <thead>
				            <tr>
				            	<th><fmt:message key="configuration.position" bundle="${localBundle}"/></th>
				                <th><fmt:message key="configuration.name" bundle="${localBundle}"/></th>
				                <th><fmt:message key="configuration.description" bundle="${localBundle}"/></th>
				                <th><fmt:message key="configuration.value" bundle="${localBundle}"/></th>
				                <th><fmt:message key="configuration.finalValue" bundle="${localBundle}"/></th>
				            </tr>
				        </thead>
				        <tbody>
							<c:forEach items="${configurationCommands}" var="configurationCommand">
					            <tr>
					            	<td>${configurationCommand.position}</td>
					                <td>${configurationCommand.name}</td>
					                <td>${configurationCommand.description}</td>
					                <td>${configurationCommand.value}</td>
					                <td>${configurationCommand.finalValue}</td>
					            </tr>
							</c:forEach>
				        </tbody>
				    </table>
				</div>
				<script type="text/javascript">
				YAHOO.util.Event.addListener(window, "load", function() {
				    YAHOO.example.EnhanceFromMarkup = function() {
				        var myColumnDefs = [
							{key:"position",label:"<fmt:message key="configuration.position" bundle="${localBundle}"/>", sortable:true},
				            {key:"name",label:"<fmt:message key="configuration.name" bundle="${localBundle}"/>", sortable:true},
				            {key:"description",label:"<fmt:message key="configuration.description" bundle="${localBundle}"/>", sortable:true, maxAutoWidth:210},
				            {key:"value",label:"<fmt:message key="configuration.value" bundle="${localBundle}"/>", sortable:true, maxAutoWidth:190},
				            {key:"finalValue",label:"<fmt:message key="configuration.finalValue" bundle="${localBundle}"/>", sortable:true, maxAutoWidth:190, 
					            editor: new YAHOO.widget.TextareaCellEditor({LABEL_CANCEL :"<fmt:message key="button.cancel" bundle="${globalBundle}"/>", LABEL_SAVE :"<fmt:message key="button.save" bundle="${globalBundle}"/>"})}
				        ];
				
				        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("configurations"));
				        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
				        myDataSource.responseSchema = {
				            fields: [{key:"position", parser:"number"},
							        {key:"name"},
				                    {key:"description"},
				                    {key:"value"},
				                    {key:"finalValue"}
				            ]
				        };
				
				        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource,
				                {sortedBy:{key:"position",dir:"desc"}}
				        );
	
				        myDataTable.subscribe("cellClickEvent", myDataTable.onEventShowCellEditor); 
	
	
				     // When cell is edited, pulse the color of the row yellow
				        var onCellEdit = function(oArgs) {
				            var elCell = oArgs.editor.getTdEl();
				            var oNewData = oArgs.newData;
	
				            record = oArgs.editor.getRecord(); 
						    YAHOO.util.Connect.asyncRequest('POST', 'index.html?name=' + record.getData('name')+'&value='+oNewData);
				        }
				        
				        myDataTable.subscribe("editorSaveEvent", onCellEdit);
	
				        
				        return {
				            oDS: myDataSource,
				            oDT: myDataTable
				        };
				    }();
				});
				</script>
			</div>							
		</div>
		<div id="ft">
			<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
		</div>
	</div>
</div>	
</body>
</html>
