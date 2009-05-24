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
	<script type="text/javascript" src="css/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="css/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="css/yui/build/datatable/datatable-min.js"></script>
	
	<script src="css/yui/build/yahoo/yahoo-min.js"></script>
	<script src="css/yui/build/event/event-min.js"></script>
	<script src="css/yui/build/connection/connection-min.js"></script>
	 
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
			    <table id="configurations">
			        <thead>
			            <tr>
			            	<th>Position</th>
			                <th>Name</th>
			                <th>Value</th>
			                <th>Description</th>
			            </tr>
			        </thead>
			        <tbody>
						<c:forEach items="${configurationCommands}" var="configurationCommand">
				            <tr>
				            	<td>${configurationCommand.position}</td>
				                <td>${configurationCommand.name}</td>
				                <td>${configurationCommand.description}</td>
				                <td>${configurationCommand.value}</td>
				                <td>${configurationCommand.finalValue}</td>
				            </tr>
						</c:forEach>
			        </tbody>
			    </table>
			</div>
			<script type="text/javascript">
			YAHOO.util.Event.addListener(window, "load", function() {
			    YAHOO.example.EnhanceFromMarkup = function() {
			        var myColumnDefs = [
						{key:"position",label:"Position", sortable:true},
			            {key:"name",label:"Name", sortable:true},
			            {key:"description",label:"Description", sortable:true},
			            {key:"value",label:"Value", sortable:true},
			            {key:"finalValue",label:"Final Value", sortable:true, editor: new YAHOO.widget.TextareaCellEditor()}
			        ];
			
			        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("configurations"));
			        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
			        myDataSource.responseSchema = {
			            fields: [{key:"position", parser:"number"},
						        {key:"name"},
			                    {key:"description"},
			                    {key:"value"},
			                    {key:"finalValue"}
			            ]
			        };
			
			        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource,
			                {caption:"Example: Progressively Enhanced Table from Markup",
			                sortedBy:{key:"position",dir:"desc"}}
			        );

			        myDataTable.subscribe("cellClickEvent", myDataTable.onEventShowCellEditor); 


			     // When cell is edited, pulse the color of the row yellow
			        var onCellEdit = function(oArgs) {
			            var elCell = oArgs.editor.getTdEl();
			            var oNewData = oArgs.newData;

			            record = oArgs.editor.getRecord(); 
					    YAHOO.util.Connect.asyncRequest('POST', 'index.html?name=' + record.getData('name')+'&value='+oNewData);
			        }
			        
			        myDataTable.subscribe("editorSaveEvent", onCellEdit);

			        
			        return {
			            oDS: myDataSource,
			            oDT: myDataTable
			        };
			    }();
			});
			</script>						
		
		</p>
	</div>
	<div id="ft">
		<p>Footer</p>
	</div>
</div>
</body>
</html>
