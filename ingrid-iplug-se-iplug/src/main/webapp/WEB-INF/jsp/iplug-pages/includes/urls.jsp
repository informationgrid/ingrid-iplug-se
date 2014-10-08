<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<div id="loading">
    <p>Urls werden geladen ...</p>
</div>

<div id="urlContent" style="visibility: hidden;">
    <fieldset>
        <legend>Filter</legend>
        <label>URL</label>
        <div class="input full space">
            <input id="filterUrl" type="text">
        </div>
        <label>Metadata</label>
        <select id="filterMetadata" multiple>
        <c:forEach items="${ metadata }" var="meta">
            <c:if test="${ meta.type != 'grouped' }">
                <optgroup label="${ meta.label }">
            </c:if>
            <c:forEach items="${ meta.children }" var="group">
                <c:if test="${ meta.type == 'grouped' }">
                    <optgroup label="${ meta.label } (${ group.label })">
                    <c:forEach items="${ group.children }" var="subgroup">
                        <option value="${ meta.id }:${ subgroup.id }" <c:forEach items="${ filterOptions }" var="filter"><c:set var="temp" value="${ meta.id }:${ subgroup.id }" /><c:if test="${ filter == temp }">selected</c:if></c:forEach>>${ subgroup.label }</option>
                    </c:forEach>
                </c:if>
                <c:if test="${ meta.type != 'grouped' }">
                    <option value="${ meta.id }:${ group.id }" <c:forEach items="${ filterOptions }" var="filter"><c:set var="temp" value="${ meta.id }:${ group.id }" /><c:if test="${ filter == temp }">selected</c:if></c:forEach>>${ group.label }</option>
                </c:if>
            </c:forEach>
        </c:forEach>
        </select>
    </fieldset>

    <!-- pager -->
    <div id="pager" class="pager">
        <form>
            <img src="../img/first.png" class="first" />
            <img src="../img/prev.png" class="prev" />
            <span class="pagedisplay"></span>
            <!-- this can be any element, including an input -->
            <img src="../img/next.png" class="next" />
            <img src="../img/last.png" class="last" />
        </form>
    </div>

    <table id="urlTable" class="data tablesorter space">
        <thead>
            <tr>
                <th data-sort="string" width="20px"></th>
                <th data-sort="string">URL</th>
                <th data-sort="string" width="60px">Status</th>
                <th data-sort="string" width="150px"></th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>

    <button id="btnDeleteUrls" type="button" name="delete" class="left">URLs l�schen</button>
    <button id="btnAddUrl" type="button" name="add" class="right">Neue URL</button>
    <div style="clear:both;"></div>


    <div id="dialog-form" title="Neue URL anlegen">
        <form>
            <fieldset>
                <div>
                    <h3>Start-URL</h3>
                    <div class="input full">                
                        <input type="text" name="startUrl" id="startUrl" value="http://" class="text ui-widget-content ui-corner-all">
                    </div>
                </div>
                
                <div>
                    <h3>Limit-URLs</h3>
                    <table id="limitUrlTable" class="data tablesorter">
                        <thead>
                            <tr>
                                <th data-sort="string">URL</th>
                                <th width="140px"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="newRow">
                                <td data-editable='false'><div class="input full"><input type="text" id="newLimitUrl"></div></td>
                                <td data-editable='false'><button id="btnAddLimitUrl" type="button">Neue Limit-URL</button></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                
                <div>
                    <h3>Exclude-URLs</h3>
                    <table id="excludeUrlTable" class="data tablesorter">
                        <thead>
                            <tr>
                                <th data-sort="string">URL</th>
                                <th width="140px"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="newRow">
                                <td data-editable='false'><div class="input full"><input type="text" id="newExcludeUrl"></div></td>
                                <td data-editable='false'><button id="btnAddExcludeUrl" type="button">Neue Exclude-URL</button></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                
                
                <!-- <h3>Metadaten</h3> -->
                
                <c:forEach items="${ metadata }" var="meta">
                    <div class="meta_${ meta.id }">
                    <h3>${ meta.label }</h3>
                    
                    <c:if test="${meta.type == 'select' || meta.type == null}" >
                        <select id="${ meta.id }" 
                            <c:if test="${ meta.isMultiple == true }">multiple</c:if>
                            <c:if test="${ meta.isDisabled == true }">disabled</c:if>
                        >
                        <c:forEach items="${ meta.children }" var="group">
                            <option value="${ meta.id }:${ group.id }" <c:if test="${ group.isDefault == true }">selected</c:if>>${ group.label }</option>
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
                <div>
                    <h3>Weitere Metadaten</h3>
                    <table id="userMetadataTable" class="data tablesorter">
                        <thead>
                            <tr>
                                <th data-sort="string">Metadata</th>
                                <th data-sort="string" width="150px"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="newRow">
                                <td data-editable='false'><div class="input full"><input id="userMeta" type="text"></div></td>
                                <td data-editable='false'><button id="btnAddUserMetadata" type="button">Benutzerdefiniertes Metadatum hinzuf�gen</button></td>
                            </tr>
                        </tbody>
                    </table>
                    <p id="userMetaError" class="error">Ein Metadatum muss aus einem Schl�ssel und einem Wert bestehen, welche durch einen Doppelpunkt getrennt sind. Bsp.: lang:de</p>
                </div>
                    
                <!-- Allow form submission with keyboard without duplicating the dialog button -->
                <!-- <input type="submit" tabindex="-1" style="position: absolute; top: -1000px"> -->
            </fieldset>
        </form>
    </div>

    <div id="dialog-confirm" title="Wirklich l�schen?">
        <p>
            <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
            M�chten Sie die Url(s) wirklich l�schen?
        </p>
    </div>

    <!-- <div id="dialog-form-limit" title="Add new Limit-URL">
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
    </div> -->
</div>
