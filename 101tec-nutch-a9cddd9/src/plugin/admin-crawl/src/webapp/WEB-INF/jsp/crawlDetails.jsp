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
	<title><fmt:message key="crawlDetails.title" bundle="${localBundle}"/></title>

	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>

	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/container-min.js"></script>
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
		<div id="yui-main" class="yui-t4">
				<div class="yui-b">
					<h3><fmt:message key="crawlDetails.segments" bundle="${localBundle}"/></h3>
					<div id="markup">
					    <table id="crawls">
					        <thead>
					            <tr>
					            	<th><fmt:message key="crawlDetails.segments" bundle="${localBundle}"/></th>
					                <th><fmt:message key="crawlDetails.size" bundle="${localBundle}"/></th>
					                <th><fmt:message key="crawlDetails.hostStats" bundle="${localBundle}"/></th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${segments}" var="segment">
						            <tr>
						            	<td>${segment.path.name}</td>
						                <td>${segment.size}</td>
						                <td><a href="statistic.html?crawlFolder=${crawlFolder}&segment=${segment.path.name}&maxCount=10"><fmt:message key="crawlDetails.hostStats" bundle="${localBundle}"/></a></td>
						            </tr>
								</c:forEach>
					        </tbody>
					    </table>
					</div>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
								{key:"path",label:"<fmt:message key="crawlDetails.segments" bundle="${localBundle}"/>", sortable:true},
					            {key:"size",label:"<fmt:message key="crawlDetails.size" bundle="${localBundle}"/>", sortable:true},
					            {key:"hostStatistic",label:"<fmt:message key="crawlDetails.hostStats" bundle="${localBundle}"/>", sortable:false}
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("crawls"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"path"},
								        {key:"size", parser:"number"},
								        {key:"hostStatistic"}
					            ]
					        };
					
					        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource, {sortedBy:{key:"path",dir:"desc"}});
					        
					        return {
					            oDS: myDataSource,
					            oDT: myDataTable
					        };
					    }();
					});
					</script>						

					<div>&nbsp;</div>
					<h3><fmt:message key="crawlDetails.database" bundle="${localBundle}"/></h3>
					<div id="markupDbs">
					    <table id="dbs">
					        <thead>
					            <tr>
					            	<th><fmt:message key="crawlDetails.database" bundle="${localBundle}"/></th>
					                <th><fmt:message key="crawlDetails.size" bundle="${localBundle}"/></th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${dbs}" var="db">
						            <tr>
						            	<td>${db.path.name}</td>
						                <td>${db.size}</td>
						            </tr>
								</c:forEach>
					        </tbody>
					    </table>
					</div>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
								{key:"path",label:"<fmt:message key="crawlDetails.database" bundle="${localBundle}"/>", sortable:true},
					            {key:"size",label:"<fmt:message key="crawlDetails.size" bundle="${localBundle}"/>", sortable:true},
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("dbs"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"path"},
								        {key:"size", parser:"number"}
					            ]
					        };
					
					        var myDataTable = new YAHOO.widget.DataTable("markupDbs", myColumnDefs, myDataSource, {sortedBy:{key:"path",dir:"desc"}});
					        
					        return {
					            oDS: myDataSource,
					            oDT: myDataTable
					        };
					    }();
					});
					</script>						

					<div>&nbsp;</div>
					<h3><fmt:message key="crawlDetails.index" bundle="${localBundle}"/></h3>
					<div id="markupIndex">
					    <table id="index">
					        <thead>
					            <tr>
					            	<th><fmt:message key="crawlDetails.index" bundle="${localBundle}"/></th>
					                <th><fmt:message key="crawlDetails.size" bundle="${localBundle}"/></th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${indexes}" var="index">
						            <tr>
						            	<td>${index.path.name}</td>
						                <td>${index.size}</td>
						            </tr>
								</c:forEach>
					        </tbody>
					    </table>
					</div>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
								{key:"path",label:"<fmt:message key="crawlDetails.index" bundle="${localBundle}"/>", sortable:true},
					            {key:"size",label:"<fmt:message key="crawlDetails.size" bundle="${localBundle}"/>", sortable:true},
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("index"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"path"},
								        {key:"size", parser:"number"}
					            ]
					        };
					
					        var myDataTable = new YAHOO.widget.DataTable("markupIndex", myColumnDefs, myDataSource, {sortedBy:{key:"path",dir:"desc"}});
					        
					        return {
					            oDS: myDataSource,
					            oDT: myDataTable
					        };
					    }();
					});
					</script>						
				
				</div>
			</div>
		</div>
		
		<div id="ft">
			<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
		</div>
	
	</div>
</div>
</body>
</html>
