<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<fieldset>
    <legend>Suche</legend>
    <form:form id="searchForm" method="post" action="instanceSearch.html">
        <input type="hidden" name="instance" value="${instance.name}">
        <label>Query</label>
        <div class="input full space">
            <input id="query" name="query" type="text" value="${query}">
        </div>
        <button class="right">Suche</button>
    </form:form>
</fieldset>

<fieldset>
    <c:if test="${empty hits}">
        <legend>Ergebnisse</legend>
        <p>keine Ergebnisse</p>
    </c:if>
    <c:if test="${!empty hits}">
        <legend>Ergebnisse 1-${hitCount} von ${totalHitCount} f&uuml;r "${query}"</legend>

        <c:forEach items="${hits}" var="hit">
            <div class="space">
               <h3>
                   <c:choose>
                       <c:when test="${details}">
                            ${hit.value['title']} (<a href="../base/searchDetails.html?id=${hit.key}">raw result</a>)
                       </c:when>
                       <c:when test="${hit.value['url'] != null && hit.value['url'] != ''}">
                           <a href="${hit.value['url']}">${hit.value['title']}</a>
                       </c:when>
                       <c:otherwise>
                           <a href="#">${hit.value['title']}</a>
                       </c:otherwise>
                   </c:choose>
               </h3>
               <span>${hit.value['abstract']}</span>
            </div>
        </c:forEach>
    </c:if>
</fieldset>