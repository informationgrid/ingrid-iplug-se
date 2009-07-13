<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Katalog URLs</title>
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
					<h3>Katalog URLs</h3>
					
					<div id="demo" class="yui-navset">
					    <ul class="yui-nav">
					        <li class="selected"><a href="listCatalogUrls.html"><em>Katalog Url's</em></a></li>
					        <li><a href="listWebUrls.html"><em>Web Url's</em></a></li>
					    </ul>            
					</div>   
				    <div>
				        <!-- Themenseite -->
						<div style="margin-top:25px"></div>
						<div>
							<img src="${theme}/gfx/add.png" align="absmiddle"/> <b><a href="createCatalogUrl.html?type=topics">Neue Themen Seite</a></b>
						</div>
						<div id="topicUrls"></div>
						<div id="topicPagination"></div>
				        
				        <script>
				        YAHOO.example.DynamicDataTopics = function() {
				            // Column definitions
				            var myColumnDefs = [ // sortable:true enables sorting
				                {key:"url", label:"Url", sortable:true},
				                {key:"created", label:"Erstellt", sortable:true},
				            ];

				            
				            // DataSource instance
				            var myDataSource = new YAHOO.util.DataSource("topicsUrlSubset.html?");
				            myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
				            myDataSource.responseSchema = {
				                resultsList: "records",
				                fields: [
				                    {key:"url"},
				                    {key:"created"},
				                ],
				                metaFields: {
				                    totalRecords: "totalRecords" // Access to value in the server response
				                }
				            };
				            
				            // DataTable configuration
				            var myConfigs = {
				                initialRequest: "startIndex=0&pageSize=10", // Initial request for first page of data
				                dynamicData: true, // Enables dynamic server-driven data
				                sortedBy : {key:"created", dir:YAHOO.widget.DataTable.CLASS_ASC}, // Sets UI initial sort arrow
				                paginator: new YAHOO.widget.Paginator({ 
					                rowsPerPage:10,
					                containers : [ "topicPagination" ], 
					                firstPageLinkLabel : "&lt;&lt; Anfang",
					                lastPageLinkLabel : "Ende &gt;&gt;",
					                nextPageLinkLabel : "Nächste &gt;",
					                previousPageLinkLabel : "&lt; Vorherige"
						                 }), // Enables pagination
				            };
				            
				            // DataTable instance
				            var myDataTable = new YAHOO.widget.DataTable("topicUrls", myColumnDefs, myDataSource, myConfigs);
				            // Update totalRecords on the fly with value from server
				            myDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
				                oPayload.totalRecords = oResponse.meta.totalRecords;
				                return oPayload;
				            }
				            
				            return {
				                ds: myDataSource,
				                dt: myDataTable
				            };
				                
				        }();						        
						</script>



				        <!-- Service Seite -->
						<div style="margin-top:25px"></div>
						<div>
							<img src="${theme}/gfx/add.png" align="absmiddle"/> <b><a href="createCatalogUrl.html?type=service">Neue Service Seite</a></b>
						</div>
						<div id="serviceUrls"></div>
						<div id="servicePagination"></div>
				        
				        <script>
				        YAHOO.example.DynamicDataTopics = function() {
				            // Column definitions
				            var myColumnDefs = [ // sortable:true enables sorting
				                {key:"url", label:"Url", sortable:true},
				                {key:"created", label:"Erstellt", sortable:true},
				            ];

				            
				            // DataSource instance
				            var myDataSource = new YAHOO.util.DataSource("serviceUrlSubset.html?");
				            myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
				            myDataSource.responseSchema = {
				                resultsList: "records",
				                fields: [
				                    {key:"url"},
				                    {key:"created"},
				                ],
				                metaFields: {
				                    totalRecords: "totalRecords" // Access to value in the server response
				                }
				            };
				            
				            // DataTable configuration
				            var myConfigs = {
				                initialRequest: "startIndex=0&pageSize=10", // Initial request for first page of data
				                dynamicData: true, // Enables dynamic server-driven data
				                sortedBy : {key:"created", dir:YAHOO.widget.DataTable.CLASS_ASC}, // Sets UI initial sort arrow
				                paginator: new YAHOO.widget.Paginator({ 
					                rowsPerPage:10,
					                containers : [ "servicePagination" ], 
					                firstPageLinkLabel : "&lt;&lt; Anfang",
					                lastPageLinkLabel : "Ende &gt;&gt;",
					                nextPageLinkLabel : "Nächste &gt;",
					                previousPageLinkLabel : "&lt; Vorherige" }), // Enables pagination
				            };
				            
				            // DataTable instance
				            var myDataTable = new YAHOO.widget.DataTable("serviceUrls", myColumnDefs, myDataSource, myConfigs);
				            // Update totalRecords on the fly with value from server
				            myDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
				                oPayload.totalRecords = oResponse.meta.totalRecords;
				                return oPayload;
				            }
				            
				            return {
				                ds: myDataSource,
				                dt: myDataTable
				            };
				                
				        }();						        
						</script>
				        
				        
				       
				        <!-- Measure Seiten -->
						<div style="margin-top:25px"></div>
						<div>
							<img src="${theme}/gfx/add.png" align="absmiddle"/> <b><a href="createCatalogUrl.html?type=measure">Neue Messwert Seite</a></b>
						</div>
						<div id="measureUrls"></div>
						<div id="measurePagination"></div>
				        
				        <script>
				        YAHOO.example.DynamicDataTopics = function() {
				            // Column definitions
				            var myColumnDefs = [ // sortable:true enables sorting
				                {key:"url", label:"Url", sortable:true},
				                {key:"created", label:"Erstellt", sortable:true},
				            ];

				            
				            // DataSource instance
				            var myDataSource = new YAHOO.util.DataSource("measureUrlSubset.html?");
				            myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
				            myDataSource.responseSchema = {
				                resultsList: "records",
				                fields: [
				                    {key:"url"},
				                    {key:"created"},
				                ],
				                metaFields: {
				                    totalRecords: "totalRecords" // Access to value in the server response
				                }
				            };
				            
				            // DataTable configuration
				            var myConfigs = {
				                initialRequest: "startIndex=0&pageSize=10", // Initial request for first page of data
				                dynamicData: true, // Enables dynamic server-driven data
				                sortedBy : {key:"created", dir:YAHOO.widget.DataTable.CLASS_ASC}, // Sets UI initial sort arrow
				                paginator: new YAHOO.widget.Paginator({ 
					                rowsPerPage:10,
					                containers : [ "measurePagination" ], 
					                firstPageLinkLabel : "&lt;&lt; Anfang",
					                lastPageLinkLabel : "Ende &gt;&gt;",
					                nextPageLinkLabel : "Nächste &gt;",
					                previousPageLinkLabel : "&lt; Vorherige" }), // Enables pagination
				            };
				            
				            // DataTable instance
				            var myDataTable = new YAHOO.widget.DataTable("measureUrls", myColumnDefs, myDataSource, myConfigs);
				            // Update totalRecords on the fly with value from server
				            myDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
				                oPayload.totalRecords = oResponse.meta.totalRecords;
				                return oPayload;
				            }
				            
				            return {
				                ds: myDataSource,
				                dt: myDataTable
				            };
				                
				        }();						        
						</script>
				        <hr/>
				        
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