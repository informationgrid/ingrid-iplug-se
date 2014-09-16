<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<form:form id="formManagement" method="post" action="../iplug-pages/instanceManagement.html">
    <input type="hidden" name="instance" value="${instance.name}" />
    <button name="start">Start Crawl</button>
</form:form>