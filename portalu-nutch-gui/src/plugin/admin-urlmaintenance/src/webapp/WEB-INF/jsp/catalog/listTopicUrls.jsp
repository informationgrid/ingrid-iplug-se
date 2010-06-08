<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
    <title>Admin URL Pflege - Katalog URLs</title>
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/container/assets/skins/sam/container.css" />
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
    
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css" />
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/datatable/assets/skins/sam/datatable.css" />
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/json/json-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/datatable/datatable-min.js"></script>
    
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo/yahoo-min.js" ></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/event/event-min.js" ></script>
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
    <link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/button/assets/skins/sam/button.css" />
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/button/button-min.js"></script>
    <script type="text/javascript" src="../theme/${theme}/js/yui/build/container/container-min.js"></script>
</head>
<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
    <div id="doc2">
        <div id="hd">
            <%@ include file="../includes/header.jsp" %>
        </div>
        
        <%@ include file="../includes/menu.jsp" %>
        
        <div id="bd">
            <div id="blocker" style="display: block; width: 100%; height: 100%; position: absolute; top: 0px; left: 0px;"></div>
            <div id="yui-main">
                <div class="yui-b">
                    <h3>Katalog URLs</h3>
                    
                    <% String selected = "topic"; %>
                    <%@ include file="menuCatalog.jsp" %>
                        
                    <div style="margin-top:25px"></div>
                    <div style="float:right">
                        <img src="../theme/${theme}/gfx/add.png" align="absmiddle"/> <b><a href="../catalog/createCatalogUrl.html?type=topics">Neue Themen Seite</a></b>
                    </div>
                    <h3>Themen Seiten</h3>
                    
                    <div id="dynamicdata">
                        <table id="myTable">
                            <thead>
                                <tr>
                                    <th>URL</th>
                                    <th>Erstellt</th>
                                    <th>Geändert</th>
                                    <th>Alt. Titel</th>
                                    <th>Thema</th>
                                    <th>Funkt. Kategorie</th>
                                    <th>Sprache</th>
                                    <th>Status</th>
                                    <th>Aktion</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="url" items="${urls}" varStatus="index">
                                <tr>
                                    <td><a href="${url.url}" target="_blank" style="color:black">${url.url}</a></td>
                                    <td><fmt:formatDate value="${url.created}" pattern="yyyy-MM-dd"/></td>
                                    <td><fmt:formatDate value="${url.updated}" pattern="yyyy-MM-dd"/></td>
                                    <td>
                                        <c:set var="i" value="-1"/>
                                        <c:forEach var="md" items="${url.metadatas}">
                                            <c:if test="${md.metadataKey == 'alt_title'}">
                                                <c:set var="i" value="${i+1}" />
                                                <c:if test="${i > 0}">, </c:if>
                                                ${md.metadataValue}
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <c:set var="i" value="-1"/>
                                        <c:forEach var="md" items="${url.metadatas}">
                                            <c:if test="${md.metadataKey == 'topic'}">
                                                <c:set var="i" value="${i+1}" />
                                                <c:if test="${i > 0}">, </c:if>
                                                <fmt:message key="${md.metadataKey}.${md.metadataValue}" />
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <c:set var="i" value="-1"/>
                                        <c:forEach var="md" items="${url.metadatas}">
                                            <c:if test="${md.metadataKey == 'funct_category'}">
                                                <c:set var="i" value="${i+1}" />
                                                <c:if test="${i > 0}">, </c:if>
                                                <fmt:message key="${md.metadataKey}.${md.metadataValue}" />
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <c:forEach var="md" items="${url.metadatas}">
                                            <c:if test="${md.metadataKey == 'lang'}">
                                                <fmt:message key="${md.metadataKey}.${md.metadataValue}" />
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>&nbsp;${url.statusAsText}</td>
                                    <td>
                                        <a href="../catalog/editCatalogUrl.html?id=${url.id}&type=topics">EDIT</a>
                                        <a href="#" id="deleteCatalogUrl_${index.index}" 
                                            onclick="document.getElementById('urlToDelete').innerHTML='${url.url}';document.getElementById('idToDelete').value='${url.id }'">DEL</a>
                                        <a href="../test.html?id=${url.id}">TEST</a>
                                    </td>
                                </tr>   
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    
                    <c:set var="label" value="URLs" scope="request"/>
                    <%@ include file="../includes/paging.jsp" %>    
                       
                    <script type="text/javascript">
                    YAHOO.util.Event.addListener(window, "load", function() {
                        YAHOO.example.EnhanceFromMarkup = function() {
                            var myColumnDefs = [
                                {key:"url", label:"Url", sortable:true},
                                {key:"created", label:"Erstellt", sortable:true},
                                {key:"edited", label:"Geändert", sortable:true},
                                {key:"altTitle", label:"Alt. Titel"},
                                {key:"topic", label:"Thema"},
                                {key:"functCategory", label:"Funkt. Kategorie"},
                                {key:"lang", label:"Sprache"},
                                {key:"status", label:"Status"},
                                {key:"action", label:"Aktion", width:100}
                            ];

                            var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("myTable"));
                            myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                            myDataSource.responseSchema = {
                                    fields: [
                                        {key:"url"},
                                        {key:"created"},
                                        {key:"edited"},
                                        {key:"altTitle"},
                                        {key:"topic"},
                                        {key:"functCategory"},
                                        {key:"lang"},
                                        {key:"status"},
                                        {key:"action"}
                                    ]
                            };
                            
                            var sortBy = '${sort}';
                            var sortDir = YAHOO.widget.DataTable.CLASS_ASC;
                            if('${dir}' == 'desc'){
                                sortDir = YAHOO.widget.DataTable.CLASS_DESC;
                            }
                            var myConfig = {
                                sortedBy : {key:sortBy, dir:sortDir}
                            }
                            var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, myDataSource, myConfig);
                            
                            return {
                                oDS: myDataSource,
                                oDT: myDataTable
                            };
                        }();
                    });
                    </script>
                        
                        <script>
                            function sort(e, data) { 
                                var fieldToSort = data[0];
                                var currentSort = data[1];
                                var currentDir = data[2];

                                dir = 'desc';
                                if(fieldToSort == currentSort){
                                    if(currentDir == 'desc'){
                                        dir = 'asc';
                                    }
                                }
                                // make page blocked during sorting
                                document.getElementById("blocker").style.display = "block";
                                window.location.href = "../catalog/listTopicUrls.html?sort=" +fieldToSort +"&dir=" +dir;
                            }
                            YAHOO.util.Event.addListener("yui-dt0-th-url-liner", "click", sort, ['url', '${sort}', '${dir}']);
                            YAHOO.util.Event.addListener("yui-dt0-th-created-liner", "click", sort, ['created', '${sort}', '${dir}']);
                            YAHOO.util.Event.addListener("yui-dt0-th-edited-liner", "click", sort, ['edited', '${sort}', '${dir}']);
                        </script>
                        
                        <div id="deleteCatalogUrlForm">
                            <div class="hd">Löschen</div>
                            <div class="bd">
                                <form method="post" action="../catalog/deleteCatalogUrl.html">
                                    <font color="red">Möchten Sie wirklich löschen?</font>
                                    <br/>
                                    <input type="hidden" name="id" id="idToDelete" value=""/>
                                    <input type="hidden" name="type" value="topics" />
                                    <span id="urlToDelete"></span>
                                </form>
                            </div>
                        </div>
                        
                        <script>
                        YAHOO.util.Event.addListener(window, "load", function() {
                            YAHOO.namespace("example.container");
                            var handleYes = function() {
                                this.form.submit();
                            };
                            
                            var handleNo = function() {
                                this.hide();
                            };

                        
                            YAHOO.example.container.deleteCatalogUrl = 
                                new YAHOO.widget.Dialog("deleteCatalogUrlForm", 
                                         { width: "300px",
                                           fixedcenter: true,
                                           visible: false,
                                           draggable: false,
                                           close: true,
                                           constraintoviewport: true,
                                           buttons: [ { text:"Löschen", handler:handleYes, isDefault:true },
                                                      { text:"Abbrechen",  handler:handleNo } ]
                                         } );
                            YAHOO.example.container.deleteCatalogUrl.render();
                            <c:forEach items="${urls}" var="url" varStatus="index">
                            YAHOO.util.Event.addListener("deleteCatalogUrl_${index.index}", "click", YAHOO.example.container.deleteCatalogUrl.show, YAHOO.example.container.deleteCatalogUrl, true);
                            </c:forEach>
                            
                            // make page available after it has been loaded completely
                            document.getElementById("blocker").style.display = "none";
                        });
                        </script>
                </div>
            </div>
        </div>
        <div id="ft">
            <%@ include file="../includes/footer.jsp" %>
        </div>
    </div>
</body>
</html>