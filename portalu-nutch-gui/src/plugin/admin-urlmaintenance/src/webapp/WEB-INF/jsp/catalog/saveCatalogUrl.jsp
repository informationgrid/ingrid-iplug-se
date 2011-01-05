<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Exclude URL</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/json/json-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
	
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/event/event-min.js" ></script>
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
					<h3>Katalog URLs</h3>
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li class="selected"><a href="../catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li><a href="../web/listWebUrls.html"><em>Web Url's</em></a></li>
					        <li><a href="../import/importer.html"><em>Importer</em></a></li>
					    </ul>            
					</div>
					<div id="subnav">
						<ul>
							<li<c:if test="${type == 'topics'}"> class="selected"</c:if>><a href="listTopicUrls.html">Themen</a></li>
							<li<c:if test="${type == 'service'}"> class="selected"</c:if>><a href="listServiceUrls.html">Service</a></li>
							<li<c:if test="${type == 'measure'}"> class="selected"</c:if>><a href="listMeasureUrls.html">Messwerte</a></li>
						</ul>
					</div>
					
					<fieldset>
						<legend>&Uuml;berpr&uuml;fen und Speichern</legend>
						<div id="markup">
						    <table id="urls">
						        <thead>
						            <tr>
						                <th>Url</th>
						                <th>Thema</th>
						                <th>Funkt. Kategorie</th>
						                <th>Rubrik</th>
						                <th>Alt. Titel</th>
						            </tr>
						        </thead>
						        <tbody>
						            <tr>
						                <td>${catalogUrlCommand.url}</td>
						                <td>
						                	<c:set var="i" value="-1"/>
						                	<c:forEach items="${catalogUrlCommand.metadatas}" var="md">
												<c:if test="${md.metadataKey == 'topic'}">
													<c:set var="i" value="${i+1}" />
													<c:if test="${i > 0}">, </c:if>
													<fmt:message key="${md.metadataKey}.${md.metadataValue}" />
												</c:if>
											</c:forEach>&nbsp;
						                </td>
						                <td>
						                	<c:set var="i" value="-1"/>
						                	<c:forEach items="${catalogUrlCommand.metadatas}" var="md">
												<c:if test="${md.metadataKey == 'funct_category'}">
													<c:set var="i" value="${i+1}" />
													<c:if test="${i > 0}">, </c:if>
													<fmt:message key="${md.metadataKey}.${md.metadataValue}" />
												</c:if>
											</c:forEach>&nbsp;
						                </td>
						                <td>
						                	<c:set var="i" value="-1"/>
						                	<c:forEach items="${catalogUrlCommand.metadatas}" var="md">
												<c:if test="${md.metadataKey == 'service' || md.metadataKey == 'measure'}">
													<c:set var="i" value="${i+1}" />
													<c:if test="${i > 0}">, </c:if>
													<fmt:message key="${md.metadataKey}.${md.metadataValue}" />
												</c:if>
											</c:forEach>&nbsp;
						                </td>
						                <td>
                                            <c:set var="i" value="-1"/>
                                            <c:forEach items="${catalogUrlCommand.metadatas}" var="md">
                                                <c:if test="${md.metadataKey == 'alt_title'}">
                                                    <c:set var="i" value="${i+1}" />
                                                    <c:if test="${i > 0}">, </c:if>
                                                    ${md.metadataValue}
                                                </c:if>
                                            </c:forEach>&nbsp;
                                        </td>
						            </tr>
						        </tbody>
						    </table>
						</div>
						
						<div style="margin-top:25px"></div>
						
						<form action="saveCatalogUrl.html" method="post">
							<c:set var="cancelUrl" value="listTopicUrls.html"/>
				            <c:choose>
				            	<c:when test="${type == 'service'}">
				            		<c:set var="cancelUrl" value="listServiceUrls.html"/>
				            	</c:when>
				            	<c:when test="${type == 'measure'}">
				            		<c:set var="cancelUrl" value="listMeasureUrls.html"/>
				            	</c:when>
				            </c:choose>
				            <input type="button" value="Abbrechen" onclick="window.location.href = '${cancelUrl}'"/>
							<input type="submit" value="Speichern">
						</form>	
					</fieldset>
					<script type="text/javascript">
					YAHOO.util.Event.addListener(window, "load", function() {
					    YAHOO.example.EnhanceFromMarkup = function() {
					        var myColumnDefs = [
					            {key:"url",label:"URL"},
					            {key:"topic",label:"Thema"},
					            {key:"functCat",label:"Funkt. Kategorie"},
					            {key:"rubric",label:"Rubrik"},
					            {key:"altTitle",label:"Alt. Titel"}
					        ];
					
					        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("urls"));
					        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
					        myDataSource.responseSchema = {
					            fields: [
									{key:"url"}, 
									{key:"topic"}, 
									{key:"functCat"}, 
									{key:"rubric"},
									{key:"altTitle"}
								]
					            
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