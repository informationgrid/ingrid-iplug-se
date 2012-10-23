<%--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="includes/include.jsp" %>
<html>
<head>
	<title><fmt:message key="search.title" bundle="${localBundle}"/></title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/paginator/paginator-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/paginator/assets/skins/sam/paginator.css"> 
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">					
		<div id="hd">
			<%@ include file="includes/header.jsp" %>
		</div>
		
		<%@ include file="includes/menu.jsp" %>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3><fmt:message key="search.headline" bundle="${localBundle}"/></h3>
					<form method="get" action="search.html" id="searchForm">
						<input type="hidden" name="start" id="start" value="0"/>
						<input type="hidden" name="length" id="length" value="10"/>
						<fieldset>
					    <legend><fmt:message key="search.testIndex" bundle="${localBundle}"/></legend>
						    <row>
						        <field>
						           <input type="text" name="query" value="${query}"/>
						        </field>
						        <desc> <input type="submit" value="<fmt:message key="button.search" bundle="${globalBundle}"/>"/></desc>
						    </row>
						</fieldset>
					</form>
					
					<c:if test="${!empty totalHits}">
					<div style="text-align:right">${totalHits} <fmt:message key="search.hitsTotal" bundle="${localBundle}"/></div>
					</c:if>
					
					<div class="result">
						<c:forEach items="${searchResults}" var="searchResult">
							<div>
								<a href="${searchResult.url}" class="searchResultTitle">${searchResult.title}</a>
							</div>
							<div class="searchResultSummary">
								${searchResult.summary}
							</div>
							<div>
								<a href="${searchResult.url}" class="searchResultUrl">${searchResult.url}</a>
							</div>
						</c:forEach>
					</div>
					
					<c:if test="${!empty totalHits && totalHits > 0}">
					<div id="paging"></div>
					<script>
					var pag = new YAHOO.widget.Paginator({
					    rowsPerPage : 10,
					    totalRecords : ${totalHits},
					    containers : "paging",
					    firstPageLinkLabel : "&lt;&lt;",
					    lastPageLinkLabel : "&gt;&gt;",
					    previousPageLinkLabel: "&lt;",
					    nextPageLinkLabel : "&gt;"
					});
					
					var Search = {
					    handlePagination : function (newState) {
					        pag.setState(newState);
							YAHOO.util.Dom.get("start").value = (newState.page - 1) * 10;
							YAHOO.util.Dom.get("searchForm").submit();
					    }
					};

					pag.setState(
						{
						    paginator    : pag,
						    page         : ${page} // the current page
						    //records      : [ 10, 19 ], // index offsets of first and last records on the current page
						    //recordOffset : 10, // index offset of the first record on the current page
						    //totalRecords : 100, // current totalRecords value
						    //rowsPerPage  : 10  // current rowsPerPage value
						}
					);
					pag.subscribe('changeRequest',Search.handlePagination);
					pag.render();
					</script>
					</c:if>
					
					
				</div>
			</div>
				<div class="yui-b">
					
				</div> 
		</div>		
		<div id="ft">
			<%@ include file="includes/footer.jsp" %>
		</div>
	</div>
</div>
</body>
</html>
