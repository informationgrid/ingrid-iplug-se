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
                    <h3>Importer</h3>
                    
                    <div class="yui-navset">
                        <ul class="yui-nav">
                            <li><a href="<%=request.getContextPath()%>/catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
                            <li><a href="<%=request.getContextPath()%>/web/listWebUrls.html"><em>Web Url's</em></a></li>
                            <li class="selected"><a href="<%=request.getContextPath()%>/import/importer.html"><em>Importer</em></a></li>
                        </ul>            
                    </div>
                    
                    <c:choose>
                        <c:when test="${state == 'failed'}"><br /><div class="error"><b>Der Import ist fehlgeschlagen.</b></div></c:when>
                        <c:when test="${state == 'succeed'}"><br /><div class="success"><b>Der Import ist gelungen.</b></div></c:when>
                    </c:choose>
                    
                    <form:form action="importer.html" method="post" enctype="multipart/form-data" modelAttribute="uploadCommand">
                        <fieldset>
                            <legend>Import</legend>
                            <row>
	                            <label>Datei:</label>
	                            <field>
		                            <input name="file" type="file" value="" /><br />
				                    <form:errors path="file" cssClass="error" element="div"/>
	                            </field>
                            </row>
                            <row>
	                            <label>Typ</label>
	                            <field>
                                    <form:radiobutton path="type" value="CATALOG"/> Katalog URLs<br />
                                    <form:radiobutton path="type" value="WEB"/> Web URLs
                                    <form:errors path="type" cssClass="error" element="div"/>
	                            </field>
                            </row>
                            <row>
                                <label>&nbsp;</label>
                                <field>
                                    <input type="submit" value="Weiter"/>
                                </field>
                            </row>
                        </fieldset>
                    </form:form>
                </div>
            </div>
        </div>
        <div id="ft">
            <%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
        </div>
    </div>
</body>
</html>