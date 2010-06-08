<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Web URLs</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/event/event-min.js" ></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/button/button-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/container/container-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
</head>
<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
	<div id="doc2">
		<div id="hd">
			<%@ include file="../includes/header.jsp" %>
		</div>
		
		<%@ include file="../includes/menu.jsp" %>
		
		<div id="bd">
      		<div id="blocker" style="display: block; width: 100%; height: 100%; position: absolute; top: 0px; left: 0px;"></div>
			<div id="yui-main">
				<div class="yui-b">
					<h3>Web URLs</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="../catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li class="selected"><a href="../web/listWebUrls.html"><em>Web Url's</em></a></li>
					        <li><a href="../import/importer.html"><em>Importer</em></a></li>
					    </ul>            
					</div>
					
					<div>
						
						<c:set var="selectedFilter" value=""/>
						<c:set var="paramString" value=""/>
						<form method="get" action="" id="filter">
						<input type="hidden" name="sort" value="${sort}"/>
						<input type="hidden" name="dir" value="${dir}"/>
						<div class="row">	
							<label>Filter Datatype:</label>
							<c:forEach var="dt" items="${datatypes }">
								<c:set var="selectedFilter" value="${selectedFilter} datatype:${dt}"/>
								<c:set var="paramString" value="${paramString}&datatype=${dt}"/>
							</c:forEach>
							<c:forEach items="${metadatas}" var="metadata">
								<c:if test="${metadata.metadataKey == 'datatype'}">
								    <input type="checkbox" id="${metadata.metadataKey}_${metadata.metadataValue}" name="${metadata.metadataKey}" value="${metadata.metadataValue}"
									<c:if test="${fn:contains(selectedFilter, metadata.metadataValue)}"> checked="checked"</c:if> /> <fmt:message key="${metadata.metadataKey}.${metadata.metadataValue}" />&nbsp;&nbsp;
									<script>
										function fnCallback(e) { document.getElementById('filter').submit() }
										YAHOO.util.Event.addListener("${metadata.metadataKey}_${metadata.metadataValue}", "click", fnCallback);
									</script>
								</c:if>
							</c:forEach>
							<div class="comment">Es werden die URLs gefiltert, die alle oben ausgewählten Eigenschaften gemeinsam besitzen.</div>
						</div>
						<div class="row">	
							<label>Filter Sprache:</label>
							<c:set var="selectedFilter" value=""/>
							<c:forEach var="l" items="${langs}">
								<c:set var="selectedFilter" value="${selectedFilter} lang:${l}"/>
								<c:set var="paramString" value="${paramString}&lang=${l}"/> 
							</c:forEach>
							<c:forEach items="${metadatas}" var="metadata">
								<c:if test="${metadata.metadataKey == 'lang'}">
								<input type="checkbox" id="${metadata.metadataKey}_${metadata.metadataValue}" name="${metadata.metadataKey}" value="${metadata.metadataValue}"
								<c:if test="${fn:contains(selectedFilter, metadata.metadataValue)}"> checked="checked"</c:if> /> <fmt:message key="${metadata.metadataKey}.${metadata.metadataValue}" />&nbsp;&nbsp;
								<script>
									function fnCallback(e) { document.getElementById('filter').submit() }
									YAHOO.util.Event.addListener("${metadata.metadataKey}_${metadata.metadataValue}", "click", fnCallback);
								</script>
								</c:if>
							</c:forEach>
							<div class="comment">Bei der Auswahl mehrerer Sprachen, werden die URLs angezeigt, die mindestens einer dieser Sprachen entsprechen.</div>
						</div>	
						</form>
						</div>
					        
					    <div style="margin-top:20px"></div>
					    
					    <div style="float:right">
							<img src="../theme/${theme}/gfx/add.png" align="absmiddle"/> <b><a href="createStartUrl.html">Neue Webseite</a></b>
						</div>
					    <h3>Web Seiten</h3>
				        <ul>
				        	<li style="float:left; list-style-type:square; color:black; margin-left:20px">Start URL</li>
				        	<li style="float:left; list-style-type:square; color:green; margin-left:20px">Limit URL</li>
				        	<li style="float:left; list-style-type:square; color:red; margin-left:20px">Exclude URL</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:50px">RV = Rechtsvorschriften</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:20px">FS = Forschungsseite</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:20px">UI = Umweltinformationen</li>
				        </ul>
				        <div id="dynamicdata" style="clear:both">
					       <table id="myTable">
					       	<thead>
					       		<tr>
						       		<th>URL</th>
						       		<th>Erstellt</th>
						       		<th>Geändert</th>
						       		<th>RV</th>
						       		<th>FS</th>
						       		<th>UI</th>
						       		<th>Sprache</th>
						       		<th>Status</th>
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
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;${url.statusAsText}</td>
						       		<td>
						       			<a href="editStartUrl.html?id=${url.id}">EDIT</a>
						       			<a href="#" id="deleteWebUrl_${index.index}" 
						       					onclick="document.getElementById('urlToDelete').innerHTML='${url.url}';document.getElementById('idToDelete').value='${url.id }'">DEL</a>
						       			<a href="../test.html?id=${url.id}">TEST</a>
						       		</td>
					       		</tr>
					       		<c:forEach var="limitUrl" items="${url.limitUrls}">
					       		<tr>
					       			<td><a href="${limitUrl.url}" target="_blank" style="color:green; font-size:11px; margin-left:10px; text-decoration:none">${limitUrl.url}</a></td>
						       		<td><fmt:formatDate value="${limitUrl.created}" pattern="yyyy-MM-dd"/></td>
						       		<td><fmt:formatDate value="${limitUrl.updated}" pattern="yyyy-MM-dd"/></td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'law')}"><img src="../theme/${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'research')}"><img src="../theme/${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'default')}"><img src="../theme/${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			<c:forEach items="${limitUrl.metadatas}" var="meta"><c:if test="${meta.metadataKey == 'lang'}"><fmt:message key="${meta.metadataKey}.${meta.metadataValue}" /></c:if></c:forEach>&nbsp;
						       		</td>
						       		<td>&nbsp;${limitUrl.statusAsText}</td>
						       		<td>&nbsp;</td>
					       		</tr>
					       		</c:forEach>
					       		<c:forEach var="excludeUrl" items="${url.excludeUrls}">
					       		<tr>
					       			<td><a href="${excludeUrl.url}" target="_blank" style="color:red; font-size:11px; margin-left:10px; text-decoration:none">${excludeUrl.url}</a></td>
						       		<td><fmt:formatDate value="${excludeUrl.created}" pattern="yyyy-MM-dd"/></td>
						       		<td><fmt:formatDate value="${excludeUrl.updated}" pattern="yyyy-MM-dd"/></td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
					       		</tr>
					       		</c:forEach>
					       		<tr>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
					       			<td>&nbsp;</td>
						       		<td>&nbsp;</td>
					       		</tr>	
					       		</c:forEach>
					       	</tbody>
					       </table>
					   </div>
						
					   <c:set var="label" value="URLs" scope="request"/>
					   <c:set var="paramString" value="${paramString}" scope="request"/>
					   <%@ include file="../includes/paging.jsp" %>     
						
				       <script type="text/javascript">
				       YAHOO.util.Event.addListener(window, "load", function() {
						    YAHOO.example.EnhanceFromMarkup = function() {
								var myColumnDefs = [
									{key:"url", label:"Url", sortable:true},
									{key:"created", label:"Erstellt", sortable:true},
									{key:"edited", label:"Geändert", sortable:true},
									{key:"isLaw", label:"RV"},
									{key:"isResearch", label:"FS"},
									{key:"isDefault", label:"UI"},
									{key:"lang", label:"Sprache"},
									{key:"status", label:"Status"},
									{key:"action", label:"Aktion", width:100}
								];
								
								var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("myTable"));
								myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
								myDataSource.responseSchema = {
										fields: [
						                    {key:"url"},
						                    {key:"created"},
						                    {key:"edited"},
						                    {key:"isLaw"},
						                    {key:"isResearch"},
						                    {key:"isDefault"},
						                    {key:"lang"},
						                    {key:"status"},
						                    {key:"action"}
						                ]
								};
						
								var sortBy = '${sort}';
								var sortDir = YAHOO.widget.DataTable.CLASS_ASC;
								if('${dir}' == 'desc'){
									sortDir = YAHOO.widget.DataTable.CLASS_DESC;
								}
								var myConfig = {
									sortedBy : {key:sortBy, dir:sortDir}
								}
								var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, myDataSource, myConfig);
								
								return {
						            oDS: myDataSource,
						            oDT: myDataTable
						        };
						    }();
						});
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
                				// make page blocked during sorting
                				document.getElementById("blocker").style.display = "block";
								window.location.href = "listWebUrls.html?sort=" +fieldToSort +"&dir=" +dir +"${paramString}";
							}
							YAHOO.util.Event.addListener("yui-dt0-th-url-liner", "click", sort, ['url', '${sort}', '${dir}']);
							YAHOO.util.Event.addListener("yui-dt0-th-created-liner", "click", sort, ['created', '${sort}', '${dir}']);
							YAHOO.util.Event.addListener("yui-dt0-th-edited-liner", "click", sort, ['edited', '${sort}', '${dir}']);
						</script>
						
						<div id="deleteWebUrlForm">
							<div class="hd">Löschen</div>
							<div class="bd">
								<form method="post" action="deleteWebUrl.html">
									<font color="red">Möchten Sie wirklich löschen?</font>
									<br/>
									<input type="hidden" name="id" id="idToDelete" value=""/>
									<span id="urlToDelete"></span>
								</form>
							</div>
						</div>
						
						<script>
						YAHOO.util.Event.addListener(window, "load", function() {
							YAHOO.namespace("example.container");
							var handleYes = function() {
							    this.form.submit();
							};
							
							var handleNo = function() {
							    this.hide();
							};
	
							YAHOO.example.container.deleteWebUrl = 
							    new YAHOO.widget.Dialog("deleteWebUrlForm", 
							             { width: "300px",
							               fixedcenter: true,
							               visible: false,
							               draggable: false,
							               close: true,
							               constraintoviewport: true,
							               buttons: [ { text:"Löschen", handler:handleYes, isDefault:true },
							                          { text:"Abbrechen",  handler:handleNo } ]
							             } );
							YAHOO.example.container.deleteWebUrl.render();
							<c:forEach items="${urls}" var="url" varStatus="index">
							YAHOO.util.Event.addListener("deleteWebUrl_${index.index}", "click", YAHOO.example.container.deleteWebUrl.show, YAHOO.example.container.deleteWebUrl, true);
							</c:forEach>
             				// make page available after it has been loaded completely
				            document.getElementById("blocker").style.display = "none";
						});
            
						</script>
					</div>
					
					
				</div>						
			</div>
		</div>
		<div id="ft">
			<%@ include file="../includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>