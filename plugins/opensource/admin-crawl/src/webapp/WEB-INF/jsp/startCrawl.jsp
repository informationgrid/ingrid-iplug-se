<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
	<title>Welcome</title>

	<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	
	<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="css/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="css/yui/build/tabview/tabview-min.js"></script>

	<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="css/yui/build/button/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="css/yui/build/container/assets/skins/sam/container.css" />
	<script type="text/javascript" src="css/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="css/yui/build/container/container-min.js"></script>
	
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
			<div id="markup">
			    <table id="crawls">
			        <thead>
			            <tr>
			            	<th>Segments</th>
			                <th>Size in Mb</th>
			            </tr>
			        </thead>
			        <tbody>
						<c:forEach items="${crawlPaths}" var="crawlPath">
				            <tr>
				            	<td>${crawlPath.path}</td>
				                <td>${crawlPath.size}</td>
				            </tr>
						</c:forEach>
			        </tbody>
			    </table>
			</div>
			<script type="text/javascript">
			YAHOO.util.Event.addListener(window, "load", function() {
			    YAHOO.example.EnhanceFromMarkup = function() {
			        var myColumnDefs = [
						{key:"path",label:"Segments", sortable:true},
			            {key:"size",label:"Size in Mb", sortable:true},
			        ];
			
			        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("crawls"));
			        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
			        myDataSource.responseSchema = {
			            fields: [{key:"path"},
						        {key:"size", parser:"number"}
			            ]
			        };
			
			        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource,
			                {caption:"Crawl Directories",
			                sortedBy:{key:"path",dir:"desc"}}
			        );
			        
			        return {
			            oDS: myDataSource,
			            oDT: myDataTable
			        };
			    }();
			});
			</script>						
		
			<form:form action="startCrawl.html" method="post" modelAttribute="crawlCommand">
				<fieldset>
					<legend>Crawl Parameters</legend>
					<form:label path="depth">Depth</form:label>
					<form:select path="depth" items="${depths}"/>
					<form:label path="topn">Top Pages</form:label>
					<form:input path="topn"/>
					<input type="hidden" name="crawlFolder" value="${crawlFolder}"/>
					<input type="submit" value="Start">	
				</fieldset>
			</form:form>

		
		</p>
	</div>
	<div id="ft">
		<p>Footer</p>
	</div>
</div>
</body>
</html>
