<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<table id="urlTable" class="data tablesorter">
    <thead>
        <tr>
            <th data-sort="string">URL</th>
            <th data-sort="string">Status</th>
            <th data-sort="string" width="150px"></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${dbUrls}" var="url" varStatus="loop">
            <tr>
                <td>${url.url}</a></td>
                <td>${url.status}</td>
                <%-- <td><button type="button" action="delete" name="delete" data-id="${instance.name}">Löschen</button></td> --%>
                <td>
                    <div>
                        <div>
                            <button class="btnUrl" data-id="${ url.id }">Bearbeiten</button>
                            <button class="select">Weitere Optionen</button>
                        </div>
                        <ul style="position:absolute; padding-left: 0; min-width: 100px;">
                            <li action="delete">Löschen</li>
                            <li action="test">Testen</li>
                        </ul>
                        <%-- <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&editUrl">Bearbeiten</a> 
                        <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&deleteUrl">Löschen</a>
                        <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&testUrl">Test</a> --%>
                    </div>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<button id="btnAddUrl" type="submit" name="add">Neue URL</button>


<div id="dialog-form" title="Neue URL anlegen">
    <form>
        <fieldset>
            <div>
                <h3>Start-URL</h3>
                <input type="text" name="startUrl" id="startUrl" value="http://" class="text ui-widget-content ui-corner-all">
            </div>
            
            <div>
                <h3>Limit-URLs</h3>
                <table id="limitUrlTable" class="data tablesorter">
                    <thead>
                        <tr>
                            <th data-sort="string">URL</th>
                            <th width="150px"></th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <button id="btnAddLimitUrl" type="button" class="right">Neue Limit-URL</button>
            </div>
            
            <div>
                <h3>Exclude-URLs</h3>
                <table id="excludeUrlTable" class="data tablesorter">
                    <thead>
                        <tr>
                            <th data-sort="string">URL</th>
                            <th width="150px"></th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <button id="btnAddLimitUrl" type="button" class="right">Neue Exclude-URL</button>
            </div>
            
            
            <!-- <h3>Metadaten</h3> -->
            
            <c:forEach items="${ metadata }" var="meta">
                <div class="meta_${ meta.id }">
                <h3>${ meta.label }</h3>
                
                <c:if test="${ meta.type == 'grouped' }" >
                    <select multiple>
                    <c:forEach items="${ meta.children }" var="group">
                        <optgroup label="${ group.label }">
                        <c:forEach items="${ group.children }" var="value">
                            <option value="${ value.id }">${ value.label } (${ value.id })</option>
                        </c:forEach>
                        </optgroup>
                    </c:forEach>
                    </select>
                </c:if>
                
                <c:if test="${meta.type == 'select' || meta.type == null}" >
                    <select <c:if test="${ meta.multiple == true }">multiple</c:if>>
                    <c:forEach items="${ meta.children }" var="group">
                        <option value="${ group.id }">${ group.label }</option>
                    </c:forEach>
                    </select>
                </c:if>
                
                <c:if test="${meta.type == 'checkbox'}" >
                    <fieldset>
                    <legend>${ meta.label }</legend>
                    <c:forEach items="${ meta.children }" var="group">
                        <label><input type="checkbox" value="${ group.id }"> ${ group.label }</label>
                    </c:forEach>
                    </fieldset>
                </c:if>
                
                <c:if test="${meta.type == 'radio'}" >
                    <c:forEach items="${ meta.children }" var="group">
                        <label><input type="radio" name="meta.id" value="${ group.id }"> ${ group.label }</label>
                    </c:forEach>
                    <div style="clear:both;"></div>
                    <c:forEach items="${ meta.children }" var="group">
                        <c:if test="${ group.children != null }">
                        <fieldset class="meta_${ group.id }" style="display: none;">
                        <c:forEach items="${ group.children }" var="check">
                            <label title="${ check.id }"><input type="checkbox" value="${ check.id }"> ${ check.label }</label>
                        </c:forEach>
                        </fieldset>
                        </c:if>
                    </c:forEach>
                </c:if>
                
                </div>
            </c:forEach>
            
            <!-- User defined metadata -->
            <table id="userMetadataTable" class="data tablesorter">
                <thead>
                    <tr>
                        <th data-sort="string">Metadata</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <input type="text" class="text ui-widget-content ui-corner-all">
            <button id="btnAddUserMetadata" type="button" class="right" >Benutzerdefinierte Metadatum hinzufügen</button>
                
            <!-- Allow form submission with keyboard without duplicating the dialog button -->
            <!-- <input type="submit" tabindex="-1" style="position: absolute; top: -1000px"> -->
        </fieldset>
    </form>
</div>

<div id="dialog-form-limit" title="Add new Limit-URL">
    <form>
        <fieldset>
            <h3>Limit-URL</h3>
            <input type="text" name="limitUrl" id="limitUrl" value="http://" class="text ui-widget-content ui-corner-all">
        </fieldset>
    </form>
</div>