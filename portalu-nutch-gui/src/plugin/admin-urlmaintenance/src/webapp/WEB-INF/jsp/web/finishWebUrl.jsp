<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
<title>Admin URL Pflege - Speichern</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="../includes/header.jsp" %>
		</div>
		
		<%@ include file="../includes/menu.jsp" %>
		
		<div id="bd">
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
					
					<fieldset>
					<c:choose>
						<c:when test="${startUrlCommand.id > -1}">
							<legend>Web Url bearbeiten - Überprüfen und Speichern</legend>
						</c:when>
						<c:otherwise>
							<legend>Web Url anlegen - Überprüfen und Speichern</legend>										
						</c:otherwise>
					</c:choose>
					<div id="markup">
					    <table id="urls">
					        <thead>
					            <tr>
					                <th>Url</th>
					                <th>&nbsp;</th>
					            </tr>
					        </thead>
					        <tbody>
					            <tr>
					                <td>${startUrlCommand.url}</td>
					                <td>&nbsp;</td>
					            </tr>
					            <c:forEach items="${startUrlCommand.limitUrlCommands}" var="limitUrl">
						            <tr>
						                <td><font style="color:green">${limitUrl.url}</font></td>
						                <td>
						                	<c:forEach var="meta" items="${limitUrl.metadatas}">
						                		<fmt:message key="${meta.metadataKey}.${meta.metadataValue}" /><br/>
						                	</c:forEach>
						                </td>
						            </tr>
					            </c:forEach>
					            <c:forEach items="${startUrlCommand.excludeUrlCommands}" var="excludeUrl">
						            <tr>
						                <td><font style="color:red">${excludeUrl.url}</font></td>
						                <td>&nbsp;</td>
						            </tr>
					            </c:forEach>
					        </tbody>
					    </table>
					</div>
					
					<row>
						<br/>
						<field>
							<form action="finishWebUrl.html" method="post">
								<input type="submit" value="Speichern">
							</form>	
						</field>
						<desc></desc>
					</row>
					</fieldset>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
					            {key:"url",label:"Urls"},
					            {key:"meta",label:""}
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("urls"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"url"}, {key:"meta"}]
					        };
					
					        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource);
					        
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
			
			</div>
		</div>
		
		<div id="ft">
			<%@ include file="../includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>