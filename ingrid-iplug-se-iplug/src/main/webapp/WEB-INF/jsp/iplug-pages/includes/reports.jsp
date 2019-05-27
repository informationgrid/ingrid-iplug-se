<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

<div id="configTabs" class="tab-container">
	<ul class="etabs">
		<li class="tab"><a href="#tab1"><em><fmt:message
						key="reports.host.title" /></em></a></li>
		<li class="tab"><a href="#tab2"><em><fmt:message
						key="reports.url_error.title" /></em></a></li>
	</ul>
	<div class="panel-container">
		<div id="tab1">
			<div id="overallStatistic">
				Insgesamt bekannt: <span class="known"></span> davon analysiert: <span
					class="fetched"></span>
			</div>
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
		</div>
	</div>
	<div id="tab2">

		<div id="loading">
			<p>Urls werden geladen ...</p>
		</div>

		<div id="urlContent" style="visibility: hidden;">
			<fieldset>
				<legend>Filter</legend>
				<label>URL</label>
				<div class="input full space">
					<input id="filterUrl" type="text"
						value="<%=request.getParameter("urlfilter") == null ? "" : request.getParameter("urlfilter")%>">
				</div>
				<label>Status</label> <select id="filterStatus" multiple>
					<c:forEach items="${ statusCodes }" var="statusCode">
						<option value="${ statusCode[0] }"
							<c:forEach items="${ statusCodes }" var="filter"><c:set var="temp" value="${ statusCode[0] }" /><c:if test="${ filter == temp }">selected</c:if></c:forEach>>${ statusCode[1] }</option>
					</c:forEach>
				</select>
				<div class="hint" onclick="$('#filterComment').toggle()">
					<span class="ui-icon ui-icon-arrow-1-e"></span>Hinweis
				</div>
				<div id="filterComment" class="comment" style="display: none;">
					Suchen Sie hier nach einer gew&uuml;nschten URL. Jede Eingabe im
					URL-Feld, aktualisiert das Ergebnis und zeigt nur URLs an, die die
					eingegebene Zeichenkette enthalten. Es k&ouml;nnen weiterhin
					beliebig viele Status Codes zum Filter hinzugef&uuml;gt werden, um
					die Suche noch mehr einzugrenzen. F&uuml;r eine schnellere
					Mehrfachauswahl muss die "Strg"-Taste gedr&uuml;ckt werden. Das
					L&ouml;schen der Status Codes erfolgt &uuml;ber das jeweilige "x"
					des Feldes. <br>
					<br> Bedeutung der Status Codes:
					<table>
						<tr>
							<td>ACCESS_DENIED</td>
							<td>Zugriff auf die URL wurde verweigert. (HTTP Fehlercode 401)</td>
						</tr>
						<tr>
							<td>EXCEPTION</td>
							<td>Es ist ein Fehler aufgetreten. (HTTP Fehlercode 50x oder anderer Fehler, z.B. Parsefehler, ung&uuml;ltige URL)</td>
						</tr>
						<tr>
							<td>GONE</td>
							<td>URL ist nicht mehr vorhanden. (HTTP Fehlercodes 400, 410)</td>
						</tr>
						<tr>
							<td>NOTFOUND</td>
							<td>Die URL wurde nicht gefunden. (HTTP Fehlercode 404)</td>
						</tr>
						<tr>
							<td>ROBOTS_DENIED</td>
							<td>Der Zugriff wurde f&uuml;r den Crawler durch robots.txt oder Meta Tags unterbunden.</td>
						</tr>
					</table>
				</div>
			</fieldset>

			<!-- pager -->
			<div id="pager" class="pager left">
				<form>
					<img src="../img/first.png" class="first" /> <img
						src="../img/prev.png" class="prev" /> <span class="pagedisplay"></span>
					<!-- this can be any element, including an input -->
					<img src="../img/next.png" class="next" /> <img
						src="../img/last.png" class="last" /> ( pro Seite: <select
						class="pagesize" style="width: 45px;">
						<option value="10">10</option>
						<option value="20">20</option>
						<option value="50">50</option>
						<option value="100">100</option>
					</select> )
				</form>
			</div>

			<table id="urlTable" class="data tablesorter space">
				<thead>
					<tr>
						<th data-sort="string" width="20px"></th>
						<th data-sort="string">URL</th>
						<th data-sort="string" width="110px">Status</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>

			<div style="clear: both;"></div>

		</div>

		<div id="waitScreen" style="display: none;">
			<div class="blocker"></div>
			<div class="text">Bitte warten ...</div>
		</div>
	</div>
</div>
</div>
