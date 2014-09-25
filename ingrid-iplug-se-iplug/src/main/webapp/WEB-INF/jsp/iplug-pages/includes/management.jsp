<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<fieldset id="statusContainer">
    <legend>Status</legend>
    <div id="crawlInfo"></div>
    <div id="status"></div>
</fieldset>

<form:form id="formManagement" method="post" action="../iplug-pages/instanceManagement.html">
    <div id="crawlStart">
        <input type="hidden" name="instance" value="${instance.name}" />
        <h3>Tiefe</h3>
        <div class="input full space"> 
            <input type="text" name="depth" value="1">
        </div>
        <h3>Anzahl der URLs</h3>
        <div class="input full space"> 
            <input type="text" name="num" value="">
        </div>
        <button name="start">Start Crawl</button>
    </div>
    <div id="crawlStop">
        <button name="stop">Crawl beenden</button>
    </div>
</form:form>