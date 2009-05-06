<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Crawl</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>

<link rel="stylesheet" type="text/css" href="css/yui/build/button/assets/skins/sam/button.css" />
<link rel="stylesheet" type="text/css" href="css/yui/build/container/assets/skins/sam/container.css" />
<script type="text/javascript" src="css/yui/build/button/button-min.js"></script>
<script type="text/javascript" src="css/yui/build/container/container-min.js"></script>

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						<div id="markup">
						    <table id="configurations">
						        <thead>
						            <tr>
						            	<th>Path</th>
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
									{key:"path",label:"Path", sortable:true},
						            {key:"size",label:"Size in Mb", sortable:true},
						        ];
						
						        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("configurations"));
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
					
						<a href="#" id="showCreateCrawl">Create New Crawl</a>
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
								               text: "Do you want to continue?",
								               icon: YAHOO.widget.SimpleDialog.ICON_HELP,
								               constraintoviewport: true,
								               buttons: [ { text:"Yes", handler:handleYes, isDefault:true },
								                          { text:"No",  handler:handleNo } ]
								             } );
								YAHOO.example.container.createCrawl.setHeader("Are you sure?");
								YAHOO.example.container.createCrawl.render();
								YAHOO.util.Event.addListener("showCreateCrawl", "click", YAHOO.example.container.createCrawl.show, YAHOO.example.container.createCrawl, true);
							}
							YAHOO.util.Event.onDOMReady(initCreateCrawl);             
						</script>
							
						<div id="createCrawl">
							<form:form method="post" action="createCrawl.html">
							</form:form>
						</div>
							
					</div>
				</div>
				<div class="yui-b">
					Crawl Navigation
					<c:forEach items="${navigations}" var="navigation">
						<a href="${navigation}">${navigation}</a>
					</c:forEach>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>