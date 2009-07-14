<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin - Konfiguration</title>
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
				<h3>Konfiguration</h3>
				
				<div style="margin-top:25px"></div>
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
				            {key:"description",label:"Description", sortable:true, maxAutoWidth:210},
				            {key:"value",label:"Value", sortable:true, maxAutoWidth:160},
				            {key:"finalValue",label:"Final Value", sortable:true, maxAutoWidth:160, editor: new YAHOO.widget.TextareaCellEditor()}
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
				                {sortedBy:{key:"position",dir:"desc"}}
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
			</div>							
		</div>
		<div id="ft">
			<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>
