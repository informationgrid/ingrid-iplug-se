<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>

{"recordsReturned":${fn:length(urls)},
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "created":"${url.created}", "type":"start"},
    		<c:forEach items="${url.limitUrls}" var="limitUrl">
	    		{"url":"${limitUrl.url}", "created":"${limitUrl.created}", "type":"limit"},
    		</c:forEach>
    		<c:forEach items="${url.excludeUrls}" var="excludeUrl">
	    		{"url":"${excludeUrl.url}","created":"${excludeUrl.created}", , "type":"exclude"},
    		</c:forEach>
    		
    		
    	</c:forEach>
    ]
}