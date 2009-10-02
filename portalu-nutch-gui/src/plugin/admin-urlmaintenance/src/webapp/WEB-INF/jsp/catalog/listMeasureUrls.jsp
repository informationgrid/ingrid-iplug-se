<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Katalog URLs</title>
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
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/container-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
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
					<h3>Katalog URLs</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li class="selected"><a href="<%=request.getContextPath()%>/catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li><a href="listWebUrls.html"><em>Web Url's</em></a></li>
					    </ul>            
					</div>
					<div id="subnav">
						<ul>
							<li><a href="<%=request.getContextPath()%>/catalog/listTopicUrls.html">Themen</a></li>
							<li><a href="<%=request.getContextPath()%>/catalog/listServiceUrls.html">Service</a></li>
							<li class="selected"><a href="<%=request.getContextPath()%>/catalog/listMeasureUrls.html">Messwerte</a></li>
						</ul>
					</div>
					   
				    <div>
						<div style="margin-top:25px"></div>
						<div style="float:right">
							<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/add.png" align="absmiddle"/> <b><a href="<%=request.getContextPath()%>/catalog/createCatalogUrl.html?type=measure">Neue Messwert Seite</a></b>
						</div>
						<h3>Messwert Seiten</h3>
						
						<div id="dynamicdata">
							<table id="myTable">
								<thead>
									<tr>
										<th>URL</th>
										<th>Erstellt</th>
										<th>Geändert</th>
										<th>Alt. Titel</th>
										<th>Rubrik</th>
										<th>Aktion</th>
									</tr>
								</thead>
								<tbody>
								<c:forEach var="url" items="${urls}" varStatus="index">
									<tr>
										<td><a href="${url.url}" target="_blank" style="color:black">${url.url}</a></td>
										<td><fmt:formatDate value="${url.created}" pattern="yyyy-MM-dd"/></td>
										<td><fmt:formatDate value="${url.updated}" pattern="yyyy-MM-dd"/></td>
										<td>&nbsp;</td>
										<td>
											<c:set var="i" value="-1"/>
											<c:forEach var="md" items="${url.metadatas}">
												<c:if test="${md.metadataKey == 'measure'}">
													<c:set var="i" value="${i+1}" />
													<c:if test="${i > 0}">, </c:if>
													${md.metadataValue}
												</c:if>
											</c:forEach>
										</td>
										<td>
											<a href="editCatalogUrl.html?id=${url.id}&type=measure">EDIT</a>
							       			<a href="#" id="deleteCatalogUrl_${index.index}" 
						       					onclick="document.getElementById('urlToDelete').innerHTML='${url.url}';document.getElementById('idToDelete').value='${url.id }'">DEL</a>
							       			<a href="test.html?id=${url.id}">TEST</a>
										</td>
									</tr>	
								</c:forEach>
								</tbody>
							</table>
						</div>
					
						<c:set var="label" value="URLs" scope="request"/>
						<%@ include file="/WEB-INF/jsp/includes/paging.jsp" %>	
						   
					    <script type="text/javascript">
							var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("myTable"));
							myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
							myDataSource.responseSchema = {
									fields: [
					                    {key:"url"},
					                    {key:"created"},
					                    {key:"edited"},
					                    {key:"altTitle"},
					                    {key:"rubric"},
					                    {key:"action"}
					                ]
							};
							
							var mySortFunction = function(a,b,desc) {
								// do nothing
								}
							
							var myColumnDefs = [
								{key:"url", label:"Url", sortable:true, sortOptions:{sortFunction:mySortFunction}},
								{key:"created", label:"Erstellt", sortable:true, sortOptions:{sortFunction:mySortFunction}},
								{key:"edited", label:"Geändert", sortable:true, sortOptions:{sortFunction:mySortFunction}},
								{key:"altTitle", label:"Alt. Titel"},
								{key:"rubric", label:"Rubrik"},
								{key:"action", label:"Aktion", width:100},
								
							];
							
							var sortBy = '${sort}';
							var sortDir = YAHOO.widget.DataTable.CLASS_ASC;
							if('${dir}' == 'desc'){
								 sortDir = YAHOO.widget.DataTable.CLASS_DESC;
								}
							var myConfig = {
								sortedBy : {key:sortBy, dir:sortDir},
							}
							var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, myDataSource, myConfig);
						</script>
						<script>
							function sort(e, data) { 
								var fieldToSort = data[0];
								var currentSort = data[1];
								var currentDir = data[2];

								dir = 'desc';
								if(fieldToSort == currentSort){
									if(currentDir == 'desc'){
										dir = 'asc';
									}
								}
								window.location.href = "listMeasureUrls.html?sort=" +fieldToSort +"&dir=" +dir;
							}
							YAHOO.util.Event.addListener("yui-dt0-th-url-liner", "click", sort, ['url', '${sort}', '${dir}']);
							YAHOO.util.Event.addListener("yui-dt0-th-created-liner", "click", sort, ['created', '${sort}', '${dir}']);
							YAHOO.util.Event.addListener("yui-dt0-th-edited-liner", "click", sort, ['edited', '${sort}', '${dir}']);
						</script>
						
						<div id="deleteCatalogUrlForm">
							<div class="hd">Löschen</div>
							<div class="bd">
								<form method="post" action="deleteCatalogUrl.html">
									<font color="red">Möchten Sie wirklich löschen?</font>
									<br/>
									<input type="hidden" name="id" id="idToDelete" value=""/>
									<input type="hidden" name="type" value="measure" />
									<span id="urlToDelete"></span>
								</form>
							</div>
						</div>
						
						<script>
						YAHOO.namespace("example.container");
						var handleYes = function() {
						    this.form.submit();
						};
						
						var handleNo = function() {
						    this.hide();
						};

						YAHOO.example.container.deleteCatalogUrl = 
						    new YAHOO.widget.Dialog("deleteCatalogUrlForm", 
						             { width: "300px",
						               fixedcenter: true,
						               visible: false,
						               draggable: false,
						               close: true,
						               constraintoviewport: true,
						               buttons: [ { text:"Löschen", handler:handleYes, isDefault:true },
						                          { text:"Abbrechen",  handler:handleNo } ]
						             } );
						YAHOO.example.container.deleteCatalogUrl.render();
						<c:forEach items="${urls}" var="url" varStatus="index">
						YAHOO.util.Event.addListener("deleteCatalogUrl_${index.index}", "click", YAHOO.example.container.deleteCatalogUrl.show, YAHOO.example.container.deleteCatalogUrl, true);
						</c:forEach>
						</script>
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