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
  <legend>Excel Datei Upload</legend>
    <div class="controls cBottom">
      <a href="#" onclick="document.location='../iplug-pages/listInstances.html';">Abbrechen</a>
      <a href="#" onclick="document.getElementById('uploadBean').submit();">Upload</a>
    </div>
    <div id="content">
      <form:form action="../iplug-pages/instanceBlpImport.html" enctype="multipart/form-data" modelAttribute="uploadBean">
              <div class="input full">
                <input type="file" name="file"/> <form:errors path="file" cssClass="error" element="div" />
                <input type="hidden" name="instance" value="<%= request.getParameter("instance") %>"/>
              </div>
      </form:form>
    </div>
</fieldset>
<fieldset id="statusContainer">
  <legend>Status</legend>
    <div id="statusContent">
      <p>Kein Import gestartet</p>
    </div>
</fieldset>
