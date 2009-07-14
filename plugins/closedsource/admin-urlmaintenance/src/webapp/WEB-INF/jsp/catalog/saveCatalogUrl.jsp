<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Exclude URL</title>
	<link rel="stylesheet" type="text/css" href="${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/css/style.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="${theme}/js/yui/build/event/event-min.js" ></script>
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">
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
					
					<fieldset>
						<legend>Überprüfen und Speichern</legend>
						<div id="markup">
						    <table id="urls">
						        <thead>
						            <tr>
						                <th>Url</th>
						            </tr>
						        </thead>
						        <tbody>
						            <tr>
						                <td>${catalogUrlCommand.url}</td>
						            </tr>
						        </tbody>
						    </table>
						</div>
						
						<div style="margin-top:25px"></div>
						
						<form action="saveCatalogUrl.html" method="post">
							<input type="submit" value="Speichern">
						</form>	
					</fieldset>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
					            {key:"url",label:"URL"},
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("urls"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [{key:"url"}]
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
			<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>