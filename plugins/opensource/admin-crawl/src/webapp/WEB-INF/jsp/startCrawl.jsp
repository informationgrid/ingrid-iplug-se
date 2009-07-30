<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Welcome</title>

	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>

	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/button/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/container/assets/skins/sam/container.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/container/container-min.js"></script>
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
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
	
	<div id="bd">
		<div id="yui-main" class="yui-t4">
				<div class="yui-b">
					<h3>Segmente</h3>
					<div id="markup">
					    <table id="crawls">
					        <thead>
					            <tr>
					            	<th>Segmente</th>
					                <th>Größe in MB</th>
					                <th>Host Statistik</th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${segments}" var="segment">
						            <tr>
						            	<td>${segment.path.name}</td>
						                <td>${segment.size}</td>
						                <td><a href="statistic.html?crawlFolder=${crawlFolder}&segment=${segment.path.name}&maxCount=10">Host Statistik</a></td>
						            </tr>
								</c:forEach>
					        </tbody>
					    </table>
					</div>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
								{key:"path",label:"Segmente", sortable:true},
					            {key:"size",label:"Größe in MB", sortable:true},
					            {key:"hostStatistic",label:"Host Statistik", sortable:false}
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
					<h3>Datenbanken</h3>
					<div id="markupDbs">
					    <table id="dbs">
					        <thead>
					            <tr>
					            	<th>Datenbank</th>
					                <th>Größe in MB</th>
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
								{key:"path",label:"Datenbank", sortable:true},
					            {key:"size",label:"Größe in MB", sortable:true},
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
