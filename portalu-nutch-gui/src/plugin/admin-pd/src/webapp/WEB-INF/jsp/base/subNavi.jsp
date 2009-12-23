<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<div id="navi_vertical">
	<div class="konf">
		<p class="no">1</p>
		<h2>Kommunikation</h2>
	</div>
	<ul>
		<li <c:if test="${active == 'communication'}">class="active"</c:if>><a href="<%=request.getContextPath()%>/base/communication.html">Kommunikation bearbeiten</a></li>
	</ul>
	<div class="konf">
		<p class="no">2</p>
		<h2>Allgemein & Datenmapping</h2>
	</div>
	<ul>

		<!-- workingDir -->
		<li <c:if test="${active == 'workingDir'}">class="active"</c:if>>
		<c:choose>
            <c:when test="${communicationExists}"><a href="<%=request.getContextPath()%>/base/workingDir.html">Arbeitsverzeichnis wählen</a></c:when>
            <c:otherwise>Arbeitsverzeichnis wählen</c:otherwise>
		</c:choose>
		</li>

		<!-- general -->
		<li <c:if test="${active == 'general'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="<%=request.getContextPath()%>/base/general.html">Angaben zu Betreiber und Datenquelle</a></c:when>
            <c:otherwise>Angaben zu Betreiber und Datenquelle</c:otherwise>
        </c:choose>
        </li>
        
		
		<!-- save -->
		<li <c:if test="${active == 'save'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="<%=request.getContextPath()%>/base/save.html">Speichern</a></c:when>
            <c:otherwise>Speichern</c:otherwise>
        </c:choose>
        </li>
		
	</ul>
	
	<div class="konf">
	   <p class="no">&nbsp;</p>
		<h2>Admin Tools</h2>
	</div>
	<ul>
	
		<!-- communication -->
		<li <c:if test="${active == 'commSetup'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${communicationExists}"><a href="<%=request.getContextPath()%>/base/commSetup.html">Kommunikations Setup</a></c:when>
            <c:otherwise>Kommunikations Setup</c:otherwise>
        </c:choose>
        </li>

		<!-- heartbeat -->
		<li <c:if test="${active == 'heartbeat'}">class="active"</c:if>>
        <c:choose>
            <c:when test="${plugdescriptionExists}"><a href="<%=request.getContextPath()%>/base/heartbeat.html">HeartBeat Setup</a></c:when>
            <c:otherwise>HeartBeat Setup</c:otherwise>
        </c:choose>
        </li>

	</ul>
	
	<div class="konf">
       <p class="no">&nbsp;</p>
        <h2>Navigation</h2>
    </div>
    <ul>

        <c:if test="${!empty instanceNavigation}">
	        <c:forEach items="${instanceNavigation}" var="navigation">
	            <c:choose>
	                <c:when test="${navigation.name == selectedInstance}">
	                    <li class="active"><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
	                    <c:if test="${!empty componentNavigation}">
				            <c:forEach items="${componentNavigation}" var="navigation">
				                <c:choose>
				                    <c:when test="${navigation.name == selectedComponent}">
				                        <li class="active">&nbsp;&nbsp;&nbsp;<a href="${navigation.link}"><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></a></li>
				                    </c:when>
				                    <c:otherwise>
				                        <li>&nbsp;&nbsp;&nbsp;<a href="${navigation.link}"><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></a></li>
				                    </c:otherwise>
				                </c:choose>
				            </c:forEach>
				        </c:if>
	                </c:when>
	                <c:otherwise>
	                    <li><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
	                </c:otherwise>
	            </c:choose>
	        </c:forEach>
        </c:if>

    </ul>
</div>