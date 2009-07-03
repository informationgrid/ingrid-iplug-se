<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="css/yui/build/tabview/assets/skins/sam/tabview.css">
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
<script type="text/javascript" src="css/yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="css/yui/build/tabview/tabview-min.js"></script>

<link rel="stylesheet" type="text/css" href="css/yui/build/paginator/assets/skins/sam/paginator.css" />
<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
<script type="text/javascript" src="css/yui/build/json/json-min.js"></script>
<script type="text/javascript" src="css/yui/build/paginator/paginator-min.js"></script>
<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>

<script type="text/javascript" src="css/yui/build/yahoo/yahoo-min.js" ></script>
<script type="text/javascript" src="css/yui/build/event/event-min.js" ></script>

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						Welcome -${partnerProviderCommand.provider}-
						
						<div id="demo" class="yui-navset">
						    <ul class="yui-nav">
						        <li><a href="listCatalogUrls.html"><em>List Catalog Url's</em></a></li>
						        <li class="selected"><a href="listWebUrls.html"><em>List Web Url</em></a></li>
						    </ul>            
						    <div class="yui-content">
						        <div><p>Willkommen in der Web Urlpflege</p></div>
						        
						        <fieldset>
						        	<label>Aktionen</label>
						        	<a href="createStartUrl.html">Neue Webseite</a>
						        	<c:forEach items="${metadatas}" var="metadata">
						        	<div id="${metadata.metadataKey}_${metadata.metadataValue}">
						        		${metadata}<input type="checkbox" >
						        	</div>
						        		<script>
											function fnCallback(e) { alert("click"); }
											YAHOO.util.Event.addListener("${metadata.metadataKey}_${metadata.metadataValue}", "click", fnCallback);
										</script>
						        	</c:forEach>
						        	
						        </fieldset>
						        
						        <div id="dynamicdata"></div>
						        
						        <script>
						        YAHOO.example.DynamicData = function() {
						            // Column definitions
						            var myColumnDefs = [ // sortable:true enables sorting
						                {key:"url", label:"Url", sortable:true},
						                {key:"created", label:"Erstellt", sortable:true},
						            ];

						            
						            // DataSource instance
						            var myDataSource = new YAHOO.util.DataSource("startUrlSubset.html?");
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
						                paginator: new YAHOO.widget.Paginator({ rowsPerPage:10 }) // Enables pagination 
						            };
						            
						            // DataTable instance
						            var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, myDataSource, myConfigs);
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

						    </div>
						</div>						
					</div>
				</div>
				<div class="yui-b">
				Welcome Navigation
					<c:forEach items="${navigations}" var="navigation">
						<a href="${navigation}">${navigation}</a>
					</c:forEach>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>