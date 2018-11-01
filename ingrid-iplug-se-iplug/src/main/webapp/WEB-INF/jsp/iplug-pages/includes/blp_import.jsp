<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
