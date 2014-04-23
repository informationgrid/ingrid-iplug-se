<c:if test="${!empty instanceNavigation}">
    <div class="yui-navset nav">
        <ul class="yui-nav">
            <c:forEach items="${instanceNavigation}" var="navigation">
                <c:choose>
                    <c:when test="${navigation.name == selectedInstance}">
                        <li class="selected"><a href="<%=rootPath%>/..${navigation.link}"><em>${navigation.name}</em></a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="<%=rootPath%>/..${navigation.link}"><em>${navigation.name}</em></a></li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ul>
    </div>
    </c:if>
    
    <c:if test="${!empty componentNavigation}">
    <div id="subnav">
        <ul>
            <c:forEach items="${componentNavigation}" var="navigation">
                <c:choose>
                    <c:when test="${navigation.name == selectedComponent}">
                        <li class="selected"><a href="<%=rootPath%>/..${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="<%=rootPath%>/..${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ul>
    </div>
</c:if>