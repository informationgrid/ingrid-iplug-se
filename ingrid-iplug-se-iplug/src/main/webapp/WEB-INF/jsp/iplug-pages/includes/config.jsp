<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

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