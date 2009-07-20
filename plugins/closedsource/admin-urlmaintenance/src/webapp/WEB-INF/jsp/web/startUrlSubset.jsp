<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>

{"recordsReturned":${fn:length(urls)},
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "created":"<fmt:formatDate value="${url.created}" pattern="yyyy-MM-dd"/>", "edited":"<fmt:formatDate value="${url.edited}" pattern="yyyy-MM-dd"/>", "type":"Start", "action" : "<a href=\"edit.html?id=${url.id}\">Bearbeiten</a> <a href=\"delete.html?id=${url.id}\">Löschen</a> <a href=\"test.html?id=${url.id}\">Testen</a>"},
    		<c:forEach items="${url.limitUrls}" var="limitUrl">
	    		{"url":"<font style=\"color:green\">${limitUrl.url}</font>", "created":"<fmt:formatDate value="${limitUrl.created}" pattern="yyyy-MM-dd"/>", "edited":"<fmt:formatDate value="${limitUrl.edited}" pattern="yyyy-MM-dd"/>", "lang":"<c:forEach items="${limitUrl.metadatas}" var="meta"><c:if test="${meta.metadataKey == 'lang'}">${meta.metadataValue}</c:if></c:forEach>", "type":"Limit", "isLaw" : "<c:if test="${fn:contains(limitUrl.metadatas,'law')}"><img src=\"${theme}/gfx/ok.png\"/></c:if>", "isResearch" : "<c:if test="${fn:contains(limitUrl.metadatas,'research')}"><img src=\"${theme}/gfx/ok.png\"/></c:if>", "isWWW" : "<c:if test="${fn:contains(limitUrl.metadatas,'www')}"><img src=\"${theme}/gfx/ok.png\"/></c:if>"},
    		</c:forEach>
    		<c:forEach items="${url.excludeUrls}" var="excludeUrl">
	    		{"url":"<font style=\"color:red\">${excludeUrl.url}</font>","created":"<fmt:formatDate value="${excludeUrl.created}" pattern="yyyy-MM-dd"/>", "edited":"<fmt:formatDate value="${excludeUrl.edited}" pattern="yyyy-MM-dd"/>", "type":"Exclude"},
    		</c:forEach>
    		{"url":"&nbsp;", "created":"&nbsp;", "type":"&nbsp;"},
    	</c:forEach>
    ]
}