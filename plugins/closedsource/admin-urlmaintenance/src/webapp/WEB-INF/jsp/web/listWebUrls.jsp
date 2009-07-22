<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Web URLs</title>
	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
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
					<h3>Web URLs</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li class="selected"><a href="listWebUrls.html"><em>Web Url's</em></a></li>
					    </ul>            
					</div>
					
					<div>
						
						<div class="row">	
							<label>Filter:</label>
							<form method="get" action="" id="filter">
								<c:forEach items="${metadatas}" var="metadata">
									<input type="checkbox" id="${metadata.metadataKey}_${metadata.metadataValue}" name="${metadata.metadataKey}" value="${metadata.metadataValue}"> ${metadata.metadataKey}:${metadata.metadataValue}&nbsp;&nbsp;
									<script>
										function fnCallback(e) { document.getElementById('filter').submit() }
										YAHOO.util.Event.addListener("${metadata.metadataKey}_${metadata.metadataValue}", "click", fnCallback);
									</script>
								</c:forEach>
							</form>
						</div>
					        
					    <div style="margin-top:20px"></div>
					    
					    <div style="float:right">
							<img src="${theme}/gfx/add.png" align="absmiddle"/> <b><a href="createStartUrl.html">Neue Webseite</a></b>
						</div>
					    <h3>Web Seiten</h3>
				        <ul>
				        	<li style="float:left; list-style-type:square; color:black; margin-left:20px">Start URL</li>
				        	<li style="float:left; list-style-type:square; color:green; margin-left:20px">Limit URL</li>
				        	<li style="float:left; list-style-type:square; color:red; margin-left:20px">Exclude URL</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:50px">RV = Rechtsvorschriften</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:20px">FS = Forschungsseite</li>
				        	<li style="float:left; list-style-type:circle; color:#666666; margin-left:20px">UT = Umweltthema</li>
				        </ul>
				        <div id="dynamicdata" style="clear:both">
					       <table id="myTable">
					       	<thead>
					       		<tr>
						       		<td>URL</td>
						       		<td>Erstellt</td>
						       		<td>Geändert</td>
						       		<td>RV</td>
						       		<td>FS</td>
						       		<td>UT</td>
						       		<td>Sprache</td>
						       		<td>Aktion</td>
					       		</tr>
					       	</thead>
					       	<tbody>
					       		<c:forEach var="url" items="${urls}">
					       		<tr>
					       			<td><a href="${url.url}" target="_blank" style="color:black">${url.url}</a></td>
						       		<td><fmt:formatDate value="${url.created}" pattern="yyyy-MM-dd"/></td>
						       		<td><fmt:formatDate value="${url.updated}" pattern="yyyy-MM-dd"/></td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>&nbsp;</td>
						       		<td>
						       			<a href="editStartUrl.html?id=${url.id}">EDIT</a>
						       			<a href="delete.html?id=${url.id}">DEL</a>
						       			<a href="test.html?id=${url.id}">TEST</a>
						       		</td>
					       		</tr>
					       		<c:forEach var="limitUrl" items="${url.limitUrls}">
					       		<tr>
					       			<td><a href="${limitUrl.url}" target="_blank" style="color:green; font-size:11px; margin-left:10px; text-decoration:none">${limitUrl.url}</a></td>
						       		<td><fmt:formatDate value="${limitUrl.created}" pattern="yyyy-MM-dd"/></td>
						       		<td><fmt:formatDate value="${limitUrl.updated}" pattern="yyyy-MM-dd"/></td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'law')}"><img src="${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'research')}"><img src="${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			&nbsp;<c:if test="${fn:contains(limitUrl.metadatas,'www')}"><img src="${theme}/gfx/ok.png"/></c:if>&nbsp;
						       		</td>
						       		<td>
						       			<c:forEach items="${limitUrl.metadatas}" var="meta"><c:if test="${meta.metadataKey == 'lang'}">${meta.metadataValue}</c:if></c:forEach>&nbsp;
						       		</td>
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
				                    {key:"isLaw"},
				                    {key:"isResearch"},
				                    {key:"isWWW"},
				                    {key:"lang"},
				                    {key:"action"}
				                ]
						};
						
						var myColumnDefs = [
							{key:"url", label:"Url", sortable:true},
							{key:"created", label:"Erstellt", sortable:true},
							{key:"edited", label:"Geändert", sortable:true},
							{key:"isLaw", label:"RV"},
							{key:"isResearch", label:"FS"},
							{key:"isWWW", label:"UT"},
							{key:"lang", label:"Sprache"},
							{key:"action", label:"Aktion"},
						];
						var myConfig = {
							sortedBy : {key:"created", dir:YAHOO.widget.DataTable.CLASS_ASC}
						}
						var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, myDataSource, myConfig);
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