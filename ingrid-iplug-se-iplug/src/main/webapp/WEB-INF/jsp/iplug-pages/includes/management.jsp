<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2021 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
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

<div id="dialog-hadoop" title="Hadoop-Log">
    <div class="content"></div>
</div>
