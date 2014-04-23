<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
    <title>PortalU URL-Pflege - Exclude URL</title>
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
                    <h3>Web URLs (${partnerProviderCommand.partner.name} / ${partnerProviderCommand.provider.name})</h3>
                    
                    <div class="yui-navset">
                        <ul class="yui-nav">
                            <li><a href="../catalog/listTopicUrls.html"><em>Katalog-URLs</em></a></li>
                            <li class="selected"><a href="../web/listWebUrls.html"><em>Web-URLs</em></a></li>
                            <li><a href="../import/importer.html"><em>Importer</em></a></li>
                        </ul>            
                    </div>
                    
                    <c:set var="maxExcludeUrls" value="${fn:length(startUrlCommand.excludeUrlCommands)}"/>
                    <c:set var="excludeUrlCounter" value="-1"/>
                    <c:forEach items="${startUrlCommand.excludeUrlCommands}" var="excludeUrl">
                        <c:set var="excludeUrlCounter" value="${excludeUrlCounter+1}"/>
                        <c:choose>
                            <c:when test="${excludeUrlCounter < maxExcludeUrls}">
                                <div class="row">
                                <form:form action="removeExcludeUrl.html" method="post" modelAttribute="startUrlCommand">
                                    ${startUrlCommand.excludeUrlCommands[excludeUrlCounter].url}
                                    <input type="hidden" name="index" value="${excludeUrlCounter}" />
                                    <input type="image" src="../theme/${theme}/gfx/delete.png" align="absmiddle" title="L&ouml;schen"/>
                                </form:form>
                                </div>
                            </c:when>
            </c:choose>
          </c:forEach>
          <form:form action="addExcludeUrl.html" method="post" modelAttribute="newExcludeUrl">
            <fieldset>
              <c:choose>
                <c:when test="${startUrlCommand.id > -1}">
                  <legend>Web-URL bearbeiten - Exclude-URL hinzuf&uuml;gen</legend>
                </c:when>
                <c:otherwise>
                  <legend>Web-URL anlegen - Exclude-URL hinzuf&uuml;gen</legend>                                        
                </c:otherwise>
              </c:choose>
              <row>
                <desc>Das Hinzuf&uuml;gen von Exclude-URLs ist optional.</desc>
              </row>
              <row>
                    <label>Exclude-URL:</label>
                    <field>
                      <c:choose>
                        <c:when test="${newExcludeUrl == null || newExcludeUrl.url == null}">
                           <input type="text" id="excludeUrl" name="excludeUrl" value="" />
                        </c:when>
                        <c:otherwise>
                           <input type="text" id="excludeUrl" name="excludeUrl" value="${newExcludeUrl.url}" />
                        </c:otherwise>
                      </c:choose>
                        <form:errors path="url" cssClass="error" element="div"/>
                    </field>
                </row>
                
                <row>
                    <label>&nbsp;</label>
                    <field>
                        <input type="submit" value="Hinzuf&uuml;gen"/>
                        <input type="button" value="Weiter" onclick="window.location.href='finishWebUrl.html'" />
                    </field>
                </row>
            </fieldset>    
            
          </form:form>
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