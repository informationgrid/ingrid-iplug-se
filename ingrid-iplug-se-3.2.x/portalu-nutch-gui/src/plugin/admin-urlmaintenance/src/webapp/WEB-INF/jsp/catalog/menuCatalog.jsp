<div id="demo" class="yui-navset">
    <ul class="yui-nav">
        <li class="selected"><a href="../catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
        <li><a href="../web/listWebUrls.html"><em>Web Url's</em></a></li>
        <li><a href="../import/importer.html"><em>Importer</em></a></li>
    </ul>            
</div>
<div id="subnav">
    <ul>
        <%
          String selectedString = "";
          if (selected.equals("topic"))
            selectedString = "<li class='selected'><a href=\"../catalog/listTopicUrls.html\">Themen</a></li><li><a href=\"../catalog/listServiceUrls.html\">Service</a></li><li><a href=\"../catalog/listMeasureUrls.html\">Messwerte</a></li>";
          else if (selected.equals("service"))
            selectedString = "<li><a href=\"../catalog/listTopicUrls.html\">Themen</a></li><li class='selected'><a href=\"../catalog/listServiceUrls.html\">Service</a></li><li><a href=\"../catalog/listMeasureUrls.html\">Messwerte</a></li>";
          else
            selectedString = "<li><a href=\"../catalog/listTopicUrls.html\">Themen</a></li><li><a href=\"../catalog/listServiceUrls.html\">Service</a></li><li class='selected'><a href=\"../catalog/listMeasureUrls.html\">Messwerte</a></li>";
            
        %>
        <%=selectedString%>
    </ul>
</div>

<div>
    <c:set var="selectedFilter" value=""/>
    <c:set var="paramString" value=""/>
    <form method="get" action="" id="filter">
    <input type="hidden" name="sort" value="${sort}"/>
    <input type="hidden" name="dir" value="${dir}"/>
   <div class="row">  
        <label>Sprachfilter:</label>
        <c:forEach var="l" items="${langs}">
            <c:set var="selectedFilter" value="${selectedFilter} lang:${l}"/>
            <c:set var="paramString" value="${paramString}&lang=${l}"/> 
        </c:forEach>
        <c:forEach items="${metadatas}" var="metadata">
            <c:if test="${metadata.metadataKey == 'lang'}">
            <input type="checkbox" id="${metadata.metadataKey}_${metadata.metadataValue}" name="${metadata.metadataKey}" value="${metadata.metadataValue}"
            <c:if test="${fn:contains(selectedFilter, metadata.metadataValue)}"> checked="checked"</c:if> /> <fmt:message key="${metadata.metadataKey}.${metadata.metadataValue}" />&nbsp;&nbsp;
            <script>
                function fnCallback(e) { document.getElementById('filter').submit() }
                YAHOO.util.Event.addListener("${metadata.metadataKey}_${metadata.metadataValue}", "click", fnCallback);
            </script>
            </c:if>
        </c:forEach>
        <div class="comment">Bei der Auswahl mehrerer Sprachen, werden die URLs angezeigt, die mindestens einer dieser Sprachen entsprechen.</div>
    </div>  
    </form>
</div>