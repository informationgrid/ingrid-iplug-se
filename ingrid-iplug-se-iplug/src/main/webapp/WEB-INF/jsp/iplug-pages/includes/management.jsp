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
            <input type="text" name="depth" value="1" required min="1" max="10" digits="true">
        </div>
        <h3>Anzahl der URLs</h3>
        <div class="input full space"> 
            <input type="text" name="num" value="" required min="1" max="1000000" digits="true">
        </div>
        <div>
            <button name="start">Start Crawl</button>
        </div>
    </div>
    <div id="crawlStop">
        <button name="stop">Crawl beenden</button>
    </div>
    
    <canvas id="myChart" width="400" height="400"></canvas>
</form:form>