<%@ include file="/WEB-INF/jsp/include.jsp" %>

{"recordsReturned":${fn:length(urls)},
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "created":"${url.created}"},
    		<c:forEach items="${url.limitUrls}" var="limitUrl">
	    		{"url":"${limitUrl.url}", "created":"${limitUrl.created}"},
    		</c:forEach>
    		<c:forEach items="${url.excludeUrls}" var="excludeUrl">
	    		{"url":"${excludeUrl.url}","created":"${excludeUrl.created}"},
    		</c:forEach>
    		
    		
    	</c:forEach>
    ]
}