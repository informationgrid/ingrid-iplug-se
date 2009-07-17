<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - Crawls</title>
	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/container/assets/skins/sam/container.css" />
	
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="${theme}/js/yui/build/event/event-min.js" ></script>
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/button/assets/skins/sam/button.css" />

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
			<div id="yui-main">
				<div class="yui-b">
					
					<div style="float:right">
						<img src="${theme}/gfx/add.png" align="absmiddle"/> <b><a href="#" id="showCreateCrawl">Neuen Crawl anlegen</a></b>
					</div>
					<h3>Crawls</h3>
					<div id="markup">
					    <table id="crawls">
					        <thead>
					            <tr>
					            	<th>Suchbar</th>
					            	<th>Status</th>
					            	<th>Pfad</th>
					                <th>Größe in MB</th>
					            </tr>
					        </thead>
					        <tbody>
								<c:forEach items="${crawlPaths}" var="crawlPath" varStatus="i">
						            <tr>
						            	<td>
						            		<div class="tumblerOff" onclick="document.getElementById('addToSearch').submit()">N</div>
						            		<div class="tumblerOn" onclick="document.getElementById('removeFromSearch').submit()">J</div>
						            		
						            		<form id="addToSearch" action="" method="post">
						            			<input type="hidden" name="crawlFolder" value="${crawlPath.path.name}"/>
						            		</form>
						            		<form id="removeFromSearch" action="" method="post">
						            			<input type="hidden" name="crawlFolder" value="${crawlPath.path.name}"/>
						            		</form>
						            	</td>
						            	<td>
						            		<a href="#" id="showStartCrawl${i.index}" onclick="document.getElementById('crawlFolder').value = '${crawlPath.path.name}'"><img src="${theme}/gfx/play.png"/></a>
						            		<!-- 
						            		<img src="${theme}/gfx/play_inactive.png"/>
						            		<img src="${theme}/gfx/loading.gif"/>
											-->
											
						            	</td>
						            	<td><a href="startCrawl.html?crawlFolder=${crawlPath.path.name}">${crawlPath.path.name}</a></td>
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
								{key:"searchable",label:"Suchbar", sortable:true},
								{key:"status",label:"Status", sortable:true},
								{key:"path",label:"Pfad", sortable:true},
					            {key:"size",label:"Größe in MB", sortable:true},
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
						               text: "Möchten Sie fortfahren?",
						               icon: YAHOO.widget.SimpleDialog.ICON_HELP,
						               constraintoviewport: true,
						               buttons: [ { text:"Ja", handler:handleYes, isDefault:true },
						                          { text:"Nein",  handler:handleNo } ]
						             } );
						YAHOO.example.container.createCrawl.setHeader("Sind Sie sicher?");
						YAHOO.example.container.createCrawl.render();
						YAHOO.util.Event.addListener("showCreateCrawl", "click", YAHOO.example.container.createCrawl.show, YAHOO.example.container.createCrawl, true);

						YAHOO.example.container.startCrawl = 
						    new YAHOO.widget.Dialog("startCrawl", 
						             { width: "500px",
						               fixedcenter: true,
						               visible: false,
						               draggable: false,
						               close: true,
						               constraintoviewport: true,
						               buttons: [ { text:"Starten", handler:handleYes, isDefault:true },
						                          { text:"Abbrechen",  handler:handleNo } ]
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
					<form:form method="post" action="">
						
						<fieldset>
							<legend>Crawl Starten</legend>
							
							<row>
								<label>Crawl</label>
								<field><input type="text" name="crawlFolder" id="crawlFolder" value="" readonly="readonly"/></field>
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
