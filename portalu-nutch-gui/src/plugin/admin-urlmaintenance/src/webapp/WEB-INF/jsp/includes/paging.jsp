<div id="paging" style="margin-top:10px">
   <c:set var="hitsPerPage" value="10"/>
   <c:forEach items="${paging.pages}" var="page" >
     	<c:choose>
			<c:when test="${page.currentPage}">
				<div class="activePage">
				<a href="?page=${page.page}&hitsPerPage=${hitsPerPage}&sort=${sort}&dir=${dir}${paramString}" class="activePage">&nbsp;${page.label}&nbsp;</a>
				</div>
			</c:when>
			<c:otherwise>
				<div class="inactivePage">
				<a href="?page=${page.page}&hitsPerPage=${hitsPerPage}&sort=${sort}&dir=${dir}${paramString}" class="inactivePage">&nbsp;${page.label}&nbsp;</a>
				</div>
			</c:otherwise>
		</c:choose>
	</c:forEach>	
	<div class="meta" style="white-space:nowrap">&nbsp; <fmt:formatNumber value="${paging.totalHits}" pattern="#,###"/> ${label}</div>
</div>