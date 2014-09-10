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
                            <button class="btnUrl">Bearbeiten</button>
                            <button class="select">Weitere Optionen</button>
                        </div>
                        <ul style="position:absolute; padding-left: 0; min-width: 100px;">
                            <li>Löschen</li>
                            <li>Testen</li>
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
                <input type="text" name="startUrl" id="startUrl" value="http://"
                    class="text ui-widget-content ui-corner-all">
            </div>
            
            <div>
                <h3>Sprache</h3>
                <select name="lang" id="langLimit">
                    <option>Deutsch</option>
                    <option>Englisch</option>
                </select>
            </div>
            
            <div style="width: 47%; float: left;">
                <h3>Partner</h3>
                <select id="partner">
                <c:forEach items="${partners}" var="partner">
                    <option value="${ partner.shortName }">${ partner.displayName }</option>
                </c:forEach>
                </select>
            </div>
            
            <div style="width: 50%;float: left;padding-left: 3%;">
                <h3>Anbieter</h3>
                <select id="provider" multiple>
                <c:forEach items="${partners}" var="partner">
                    <c:forEach items="${partner.provider}" var="provider">
                        <option class="${partner.shortName}" value="${ provider.shortName }">${ provider.displayName }</option>
                    </c:forEach>
                </c:forEach>
                </select>
            </div>
            
            <div>
                <h3>Limit-URLs</h3>
                <table id="limitUrlTable" class="data tablesorter">
                    <thead>
                        <tr>
                            <th data-sort="string">URL</th>
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
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <button id="btnAddLimitUrl" type="button" class="right">Neue Exclude-URL</button>
            </div>
            
            
            <div>
                <h3>Metadaten</h3>
                <!-- <select id="type"> -->
                <c:forEach items="${types}" var="type">
                    <%-- <option value="${ type.id}">${ type.name }</option> --%>
                    <label><input type="radio" name="type" value="${ type.id }"> ${ type.name }</label>
                </c:forEach>
                <div style="clear:both;"></div>
                <!-- </select> -->
                <fieldset id="metadata">
                    <c:forEach items="${types}" var="type">
                        <c:forEach items="${type.options}" var="option">
                            <span class="${type.id}">
                                <label><input type="checkbox" value="${option.id}">${ option.value }</label>
                            </span>
                        </c:forEach>
                    </c:forEach>
                </fieldset>
            </div>
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