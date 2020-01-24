<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
    <%@ page contentType="text/html; charset=UTF-8" %>
      <div class="hint" onclick="$('#filterComment').toggle()"><span class="ui-icon ui-icon-arrow-1-e"></span>Hinweise</div>
      <div id="filterComment" class="comment" style="display: none;">
        <ul>
          <li>Alle bestehenden URLs der Instanz werden bei einem Import gelöscht.</li>
          <li>Sollte es Fehler während des Imports geben, werden diese im Import log angezeigt, sodass das Excelfile entsprechend korrigiert werden kann.</li>
          <li>Nach dem erfolgreichen Import, muss im 'Management' Tab ein Crawl angestoßen werden. (Empfohlene Parameter: Tiefe: 1, Anzahl der URLs: 10.000)</li>
          <li>Anschließend finden sich im Tab 'Reports' Informationen zu Problemen beim Crawlen der URLs. Fehler könnten hier Auswirkungen auf die Darstellung der Marker haben.</li>
          <li>Enthält die fehlerhafte URL in der URL Pflege Marker Informationen (Eigenschaft: blp_marker:blp_marker), wird kein Marker dargestellt.</li>
        </ul>
      </div>

      <fieldset>
        <legend>Import Parameter</legend>
        <div id="content">
          <form:form action="../iplug-pages/instanceBlpImport.html" enctype="multipart/form-data" modelAttribute="blpImportBean">

            <row>
              <label>
                Partner:
              </label>
              <field>
                <div class="input full">
                  <form:select path="partner">
                    <form:options items="${partners}"/>
                  </form:select>
                </div>
              </field>
              <desc></desc>
            </row>

            <row>
              <label>
                Excel Datei:
              </label>
              <field>
                <div class="input full">
                  <input type="file" name="file"/>
                  <form:errors path="file" cssClass="error" element="div"/>
                  <input type="hidden" name="instance" value="<%= request.getParameter("instance") %>"/>
                </div>
              </field>
              <desc></desc>
            </row>
          </form:form>
          <row>
            <field>
              <div class="controls cBottom">
                <!--<a href="#" onclick="document.location='../iplug-pages/listInstances.html';">Abbrechen</a>-->
                <a href="#" onclick="document.getElementById('blpImportBean').submit();">Upload</a>
              </div>
            </field>
            <desc></desc>
          </row>
        </fieldset>

      </div>
      <fieldset id="statusContainer">
        <legend>Status</legend>
        <div id="importInfo" class="space"></div>
        <div id="allInfo">
            <div id="statusContent">
              <p>Kein Import gestartet</p>
            </div>
            <div id="moreInfo" style="padding-top: 10px;">weitere Informationen: <a href='#' onclick='showDetailedImportLog()'>import.log</a></div>
        </div>
      </fieldset>

      <div id="dialog-detailed" title="Import Log">
          <div class="content"></div>
      </div>
