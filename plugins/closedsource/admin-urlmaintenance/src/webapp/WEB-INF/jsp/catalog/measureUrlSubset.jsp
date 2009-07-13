<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
{"recordsReturned":${fn:length(urls)},
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "created":"${url.created}"},
    	</c:forEach>
    ]
}