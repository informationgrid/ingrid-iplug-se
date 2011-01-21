<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
    <title>PortalU URL-Pflege - Limit URLs</title>
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
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
                            <li><a href="../catalog/listTopicUrls.html"><em>Katalog-URLs</em></a></li>
                            <li class="selected"><a href="../web/listWebUrls.html"><em>Web-URLs</em></a></li>
                            <li><a href="../import/importer.html"><em>Importer</em></a></li>
                        </ul>            
                    </div>
                    
                    <c:set var="maxLimitUrls" value="${fn:length(startUrlCommand.limitUrlCommands)}"/>
                    <c:set var="limitUrlCounter" value="-1"/>
                    <c:forEach items="${startUrlCommand.limitUrlCommands}" var="limitUrl">
                        <c:set var="limitUrlCounter" value="${limitUrlCounter+1}"/>
                        <c:choose>
                            <c:when test="${limitUrlCounter < maxLimitUrls}">
                                <div class="row">
                                <form:form id="limitList" action="removeLimitUrl.html" method="post" modelAttribute="startUrlCommand">
                                    ${startUrlCommand.limitUrlCommands[limitUrlCounter].url}
                                    (<c:forEach var="metadata" items="${startUrlCommand.limitUrlCommands[limitUrlCounter].metadatas}">
                                        <fmt:message key="${metadata.metadataKey}.${metadata.metadataValue}" />
                                    </c:forEach>)
                                    <input type="hidden" name="index" value="${limitUrlCounter}" />
                                    <input type="image" src="../theme/${theme}/gfx/delete.png" align="absmiddle" title="L&ouml;schen"/>
                                </form:form>
                                </div>
                            </c:when>
                        </c:choose>
                    </c:forEach>
          <form:form id="newLimit" action="addLimitUrl.html" method="post" modelAttribute="newLimitUrl">
            <fieldset>
              <c:choose>
                <c:when test="${startUrlCommand.id > -1}">
                  <legend>Web-URL bearbeiten - Limit-URL hinzuf&uuml;gen</legend>
                </c:when>
                <c:otherwise>
                  <legend>Web-URL anlegen - Limit-URL hinzuf&uuml;gen</legend>                                      
                </c:otherwise>
              </c:choose>
              <row>
                    <label>Limit-URL:</label>
                    <field>
                       <input type="text" id="limitUrl" name="limitUrl" value="${startUrlCommand.url}" />
                       <form:errors path="url" cssClass="error" element="div"/>
                    </field>
                    <desc></desc>
                </row>
                
                <row>
                    <label>Sprache:</label>
                    <field>
                        <select name="metadatas" >
                            <c:forEach var="lang" items="${langs}">
                                <option value="${lang.id}"><fmt:message key="${lang.metadataKey}.${lang.metadataValue}" /></option>
                            </c:forEach>
                        </select>
                    </field>
                    <desc></desc>
                </row>
                
                <row>
                    <label>Typ:</label>
                        <field>
                            <c:forEach var="type" items="${datatypes}">
                                <input type="checkbox" name="metadatas" value="${type.id}" /><fmt:message key="${type.metadataKey}.${type.metadataValue}" /><br/>
                            </c:forEach>
                        </field>
                    <desc></desc>
                </row>
                
                <row>
                    <form:errors path="provider" cssClass="error" element="div"/>
                    <form:errors path="metadatas" cssClass="error" element="div"/>
                </row>
                
                <row>
                    <label>&nbsp;</label>
                    <field>
                        <input type="submit" value="Hinzuf&uuml;gen"/>
                        <c:if test="${maxLimitUrls > 0}">
                          <input type="button" value="Weiter" onclick="window.location.href='addExcludeUrl.html'" />
                      </c:if>
                    </field>
                </row>
                
              </fieldset>
            
          </form:form>
                        
                    
                    
                </div>
                
                <div class="yui-b">
                
                </div>
            </div>
        </div>
        
        <div id="ft">
            <%@ include file="../includes/footer.jsp" %>
        </div>
        
    </div>
</body>
</html>