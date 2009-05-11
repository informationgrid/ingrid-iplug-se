<%@ include file="/WEB-INF/jsp/include.jsp" %>

{"recordsReturned":3,
    "totalRecords":${count},
    "records":[
    	<c:forEach items="${urls}" var="url">
    		{"url":"${url.url}", "timeStamp":"${url.timeStamp}"},
    		<c:forEach items="${url.limitUrls}" var="limitUrl">
	    		{"url":"${limitUrl.url}", "timeStamp":"${limitUrl.timeStamp}"},
	    		<c:forEach items="${url.excludeUrls}" var="excludeUrl">
		    		{"url":"${excludeUrl.url}","timeStamp":"${excludeUrl.timeStamp}"},
	    		</c:forEach>
    		</c:forEach>
    		
    	</c:forEach>
    ]
}