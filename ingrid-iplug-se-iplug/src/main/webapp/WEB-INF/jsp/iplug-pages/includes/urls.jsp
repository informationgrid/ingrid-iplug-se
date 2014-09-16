<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<fieldset>
    <legend>Filter</legend>
    <label>Metadata</label>
    <select id="filterMetadata" multiple>
    <c:forEach items="${ metadata }" var="meta">
        <optgroup label="${ meta.label }">
        <c:forEach items="${ meta.children }" var="group">
            <option value="${ meta.id }:${ group.id }" <c:forEach items="${ filterOptions }" var="filter"><c:set var="temp" value="${ meta.id }:${ group.id }" /><c:if test="${ filter == temp }">selected</c:if></c:forEach>>${ group.label }</option>
        </c:forEach>
    </c:forEach>
    </select>
</fieldset>

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
                            <li action="template">Als Template verwenden ...</li>
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
                <button id="btnAddExcludeUrl" type="button" class="right">Neue Exclude-URL</button>
            </div>
            
            
            <!-- <h3>Metadaten</h3> -->
            
            <c:forEach items="${ metadata }" var="meta">
                <div class="meta_${ meta.id }">
                <h3>${ meta.label }</h3>
                
                <c:if test="${meta.type == 'select' || meta.type == null}" >
                    <select id="${ meta.id }" <c:if test="${ meta.multiple == true }">multiple</c:if>>
                    <c:forEach items="${ meta.children }" var="group">
                        <option value="${ meta.id }:${ group.id }">${ group.label }</option>
                    </c:forEach>
                    </select>
                </c:if>

                <c:if test="${ meta.type == 'grouped' }" >
                    <select id="${ meta.id }" multiple>
                    <c:forEach items="${ meta.children }" var="group">
                        <optgroup label="${ group.label }">
                        <c:forEach items="${ group.children }" var="value">
                            <option value="${ meta.id }:${ value.id }">${ value.label }</option>
                        </c:forEach>
                        </optgroup>
                    </c:forEach>
                    </select>
                </c:if>
                
                <c:if test="${meta.type == 'checkbox'}" >
                    <fieldset>
                    <legend>${ meta.label }</legend>
                    <c:forEach items="${ meta.children }" var="group">
                        <label><input type="checkbox" value="${ meta.id }:${ group.id }"> ${ group.label }</label>
                    </c:forEach>
                    </fieldset>
                </c:if>
                
                <c:if test="${meta.type == 'radio'}" >
                    <c:forEach items="${ meta.children }" var="group">
                        <label><input type="radio" name="meta.id" value="${ meta.id }:${ group.id }"> ${ group.label }</label>
                    </c:forEach>
                    <div style="clear:both;"></div>
                    <c:forEach items="${ meta.children }" var="group">
                        <c:if test="${ group.children != null }">
                        <fieldset class="meta_${ group.id }" style="display: none;">
                        <c:forEach items="${ group.children }" var="check">
                            <label title="${ check.id }"><input type="checkbox" value="${ meta.id }:${ check.id }"> ${ check.label }</label>
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
                        <th data-sort="string" width="80px"></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <input id="userMeta" type="text" class="text ui-widget-content ui-corner-all">
            <p id="userMetaError" class="error">Ein Metadatum muss aus einem Schlüssel und einem Wert bestehen, welche durch einen Doppelpunkt getrennt sind. Bsp.: lang:de</p>
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
<div id="dialog-form-exclude" title="Add new Exclude-URL">
    <form>
        <fieldset>
            <h3>Exclude-URL</h3>
            <input type="text" name="excludeUrl" id="excludeUrl" value="http://" class="text ui-widget-content ui-corner-all">
        </fieldset>
    </form>
</div>