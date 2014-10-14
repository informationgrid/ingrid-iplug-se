<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<form:form id="formManagement" method="post" action="../iplug-pages/instanceManagement.html">
    <div id="crawlStart">
        <input type="hidden" name="instance" value="${instance.name}" />
        <div style="width: 50%; float:left;">
            <div style="padding-right:20px;">
                <h3>Tiefe</h3>
                <div class="input full space" style="margin-right: 20px;"> 
                    <input type="text" name="depth" value="1" required min="1" max="10" digits="true">
                </div>
            </div>
        </div>
        <div style="width: 50%; float:left;">
            <h3>Anzahl der URLs</h3>
            <div class="input full space"> 
                <input type="text" name="num" value="" required min="1" max="1000000" digits="true">
            </div>
        </div>
        <div class="space">
            <button name="start">Start Crawl</button>
        </div>
    </div>
    <div id="crawlStop" class="space">
        <button name="stop">Crawl beenden</button>
    </div>
</form:form>

<fieldset id="statusContainer">
    <legend>Status</legend>
    <div id="crawlInfo" class="space"></div>
    <div id="allInfo">
        <div id="status"></div>
        <div id="moreInfo" style="padding-top: 10px;">weitere Informationen: <a href='#' onclick='showHadoopLog()'>hadoop.log</a></div>
    </div>
</fieldset>

<div id="overallStatistic">Insgesamt bekannt: <span class="known"></span> davon analysiert: <span class="fetched"></span></div>
<table id="statisticTable" class="data tablesorter">
    <thead>
        <tr>
            <th width="100px"></th>
            <th data-sort="string">Host</th>
            <th data-sort="int" width="70px">Bekannt</th>
            <th data-sort="int" width="70px">Analysiert</th>
            <th data-sort="int" width="70px">Ratio</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
<!-- <canvas id="myChart" width="700" height="400"></canvas> -->

<div id="dialog-hadoop" title="Hadoop-Log">
    <div class="content"></div>
</div>