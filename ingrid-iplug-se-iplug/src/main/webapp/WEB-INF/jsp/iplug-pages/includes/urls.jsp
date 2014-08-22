<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<table id="urlTable" class="data tablesorter">
    <thead>
        <tr>
            <th data-sort="string">URL</th>
            <th data-sort="string">Status</th>
            <th data-sort="string"></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${dbUrls}" var="url" varStatus="loop">
            <tr>
                <td>${url.url}</a></td>
                <td>${url.status}</td>
                <%-- <td><button type="button" action="delete" name="delete" data-id="${instance.name}">Löschen</button></td> --%>
                <td>
                    <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&editUrl">Bearbeiten</a> 
                    <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&deleteUrl">Löschen</a>
                    <a href="instanceUrls.html?instance=${instance.name}&id=${url.id}&testUrl">Test</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<button type="submit" name="add">Neue URL</button>