<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
    <title>Admin URL Pflege - Importer</title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/json/json-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/event/event-min.js" ></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/button-min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/container-min.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
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
                            <li class="selected"><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
                        </c:when>
                        <c:otherwise>
                            <li><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ul>
        </div>
        </c:if>
        
        <div id="bd">
            <div id="yui-main">
                <div class="yui-b">
                    <h3>Importierte URLs</h3>
                    
                    <div class="yui-navset">
                        <ul class="yui-nav">
                            <li><a href="<%=request.getContextPath()%>/catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
                            <li><a href="<%=request.getContextPath()%>/web/listWebUrls.html"><em>Web Url's</em></a></li>
                            <li class="selected"><a href="<%=request.getContextPath()%>/import/importer.html"><em>Importer</em></a></li>
                        </ul>            
                    </div>
                    <br />
                    <div id="markup"><table id="configurations">
                        <thead>
                            <tr>
                                <c:choose>
                                    <c:when test="${containerCommand.type == 'WEB'}">
		                                <th>Star URL</th>
		                                <th>Limit URLs</th>
		                                <th>Exclude URLs</th>
                                    </c:when>
                                    <c:otherwise>
                                        <th>URL</th>
                                    </c:otherwise>
                                </c:choose>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
		                    <c:forEach items="${containerCommand.containers}" var="container">
		                        <tr> 
		                            <c:choose>
                                        <c:when test="${containerCommand.type == 'WEB'}">
                                            <td>
                                            <c:choose>
		                                        <c:when test="${empty container.value}"><a href="${container.key.startUrl.url}" target="_blank" style="color: green">${container.key.startUrl.url}</a></c:when>
		                                        <c:otherwise><span class="error">${container.key.startUrl.url}</span></c:otherwise>
		                                    </c:choose>
                                            </td>
                                            <td><c:forEach items="${container.key.whiteUrls}" var="whiteUrl">${whiteUrl.url}<br /></c:forEach></td>
				                            <td><c:forEach items="${container.key.blackUrls}" var="blackUrls">${blackUrls.url}<br /></c:forEach></td>
			                            </c:when>
	                                    <c:otherwise>
	                                        <td>
	                                        <c:choose>
                                                <c:when test="${empty container.value}"><c:forEach items="${container.key.whiteUrls}" var="whiteUrl"><a href="${whiteUrl.url}" target="_blank" style="color: green">${whiteUrl.url}</a><br /></c:forEach></c:when>
                                                <c:otherwise><c:forEach items="${container.key.whiteUrls}" var="whiteUrl"><span class="error">${whiteUrl.url}</span><br /></c:forEach></c:otherwise>
                                            </c:choose>
	                                        </td>
	                                    </c:otherwise>
	                                </c:choose>
	                                <td>
	                                   <c:if test="${empty container.value}"><span class="success">OK</span><br /></c:if>
	                                   <c:if test="${!empty container.value['provider.empty']}"><span class="error">fehlender Anbieter</span><br /></c:if>
	                                   <c:if test="${!empty container.value['provider.invalid']}"><span class="error">ungültiger Anbieter</span><br /></c:if>
	                                   <c:if test="${!empty container.value['whiteurl.empty']}"><span class="error">fehlende Limit URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['whiteurl.reduntant']}"><span class="error">redundante Limit URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['whiteurl.duple']}"><span class="error">doppelte Limit URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['metadata.empty']}"><span class="error">fehlende Metadaten</span><br /></c:if>
	                                   <c:if test="${!empty container.value['metadata.invalid']}"><span class="error">ungültige Metadaten</span><br /></c:if>
	                                   <c:if test="${!empty container.value['blackurl.inalid']}"><span class="error">ungültige Exclude URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['starturl.empty']}"><span class="error">fehlende Start URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['starturl.invalid']}"><span class="error">ungültige Start URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['starturl.duple']}"><span class="error">doppelte Start URL</span><br /></c:if>
	                                   <c:if test="${!empty container.value['whiteurl.empty']}"><span class="error">fehlende Limit URL</span><br /></c:if>
	                                </td>
		                        </tr>
		                    </c:forEach>
                        </tbody>
                    </table></div>
                    <script type="text/javascript">
	                YAHOO.util.Event.addListener(window, "load", function() {
	                    YAHOO.example.EnhanceFromMarkup = function() {
	                        var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("configurations"));
	                        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
	                        
							<c:choose>
								<c:when test="${containerCommand.type == 'WEB'}">
			                        var myColumnDefs = [
			                	                        
			                            {key:"startUrl",label:"Start URL", sortable:true},
			                            {key:"limitUrl",label:"Limit URLs", sortable:true},
			                            {key:"excludeUrl",label:"Exclude URLs", sortable:true},
			                            {key:"state",label:"Status", sortable:true}
			                        ];
			                        myDataSource.responseSchema = {
			                            fields: [
		                                    {key:"startUrl"},
			                                {key:"limitUrl"},
			                                {key:"excludeUrl"},
			                                {key:"state"}
			                            ]
			                        };
								</c:when>
								<c:otherwise>
								    var myColumnDefs = [
                                        {key:"url",label:"URL", sortable:true},
                                        {key:"state",label:"Status", sortable:true}
			                        ];
							        myDataSource.responseSchema = {
		                                 fields: [
		                                     {key:"url"},
		                                     {key:"state"}
		                                 ]
		                            };
								</c:otherwise>
							</c:choose>
	                
	                        var myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, myDataSource, {});
	                        
	                        return {
	                            oDS: myDataSource,
	                            oDT: myDataTable
	                        };
	                    }();
	                });
	                </script>
                    <br />
                    <c:choose>
                        <c:when test="${containerCommand.isValid}">
                            <form action="" method="post">
                                <input type="submit" value="Speichern" />
                            </form>
                        </c:when>
                        <c:otherwise><button onclick="document.location='<%=request.getContextPath()%>/import/importer.html'">Abbrechen</button></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div id="ft">
            <%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
        </div>
    </div>
</body>
</html>