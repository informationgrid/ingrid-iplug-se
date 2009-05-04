<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />

<link rel="stylesheet" type="text/css" href="css/yui/build/datatable/assets/skins/sam/datatable.css" />
<script type="text/javascript" src="css/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>

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
						                <th>Name</th>
						                <th>Value</th>
						                <th>Description</th>
						            </tr>
						        </thead>
						        <tbody>
									<c:forEach items="${configurationCommands}" var="configurationCommand">
							            <tr>
							                <td>${configurationCommand.name}</td>
							                <td>${configurationCommand.value}</td>
							                <td>${configurationCommand.description}</td>
							            </tr>
									</c:forEach>
						        </tbody>
						    </table>
						</div>
						<script type="text/javascript">
						YAHOO.util.Event.addListener(window, "load", function() {
						    YAHOO.example.EnhanceFromMarkup = function() {
						        var myColumnDefs = [
						            {key:"name",label:"Name", sortable:true},
						            {key:"value",label:"Value", sortable:true},
						            {key:"description",label:"Description", sortable:true},
						        ];
						
						        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("configurations"));
						        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
						        myDataSource.responseSchema = {
						            fields: [{key:"name"},
						                    {key:"value"},
						                    {key:"description"},
						            ]
						        };
						
						        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource,
						                {caption:"Example: Progressively Enhanced Table from Markup",
						                sortedBy:{key:"name",dir:"desc"}}
						        );
						        
						        return {
						            oDS: myDataSource,
						            oDT: myDataTable
						        };
						    }();
						});
						</script>						
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