<%@ include file="/WEB-INF/jsp/base/include.jsp"%>


<div id="configTabs" class="tab-container">
    <ul class="etabs">
        <li class="tab"><a href="#tab1"><em><fmt:message key="configuration.metadata.title" /></em></a></li>
        <li class="tab"><a href="#tab2"><em><fmt:message key="configuration.nutch.title" /></em></a></li>
    </ul>
    <div class="panel-container">
        <div id="tab1">
            Metadata
            <textarea id="metadata" rows="50" cols="100">${ metaConfigJson }</textarea>
            <button id="btnUpdateMetadata">Speichern</button>
        </div>
        <div id="tab2">
        
            <table id="configurationTable" class="data tablesorter">
            	<thead>
            		<tr>
            			<th data-sort="string" style="width: 35px;">Pos</th>
            			<th data-sort="string" style="width: 150px;">Name</th>
            			<th data-sort="string">Beschreibung</th>
            			<th data-sort="string">Standard Wert</th>
            			<th data-sort="string">Eigener Wert</th>
            		</tr>
            	</thead>
            	<tbody>
            		<c:forEach items="${configurationCommands}" var="command" varStatus="loop">
            			<tr>
            				<td data-editable='false'>${command.position}</a></td>
            				<td data-editable='false'>${command.name}</td>
            				<td data-editable='false'>${command.description}</td>
            				<td data-editable='false'>${command.value}</td>
            				<td>${command.finalValue}</td>
            			</tr>
            		</c:forEach>
            	</tbody>
            </table>
        </div>
    </div>
</div>