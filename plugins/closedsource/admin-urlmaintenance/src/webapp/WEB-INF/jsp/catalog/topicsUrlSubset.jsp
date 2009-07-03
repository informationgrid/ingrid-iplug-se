<%@ include file="/WEB-INF/jsp/include.jsp" %>

{"recordsReturned":3,
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "timeStamp":"${url.timeStamp}"},
    	</c:forEach>
    ]
}