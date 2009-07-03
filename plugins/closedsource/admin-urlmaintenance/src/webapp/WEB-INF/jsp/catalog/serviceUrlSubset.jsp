<%@ include file="/WEB-INF/jsp/include.jsp" %>

{"recordsReturned":${fn:length(urls)},
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "created":"${url.created}"},
    	</c:forEach>
    ]
}