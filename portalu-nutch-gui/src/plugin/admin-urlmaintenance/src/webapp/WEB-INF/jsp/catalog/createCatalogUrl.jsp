<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Exclude URL</title>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="/WEB-INF/jsp/includes/header.jsp" %>
		</div>
		
		<c:if test="${!empty instanceNavigation}">
		<div class="yui-navset nav">
		    <ul class="yui-nav">
				<c:forEach items="${instanceNavigation}" var="navigation">
					<c:choose>
						<c:when test="${navigation.name == selectedInstance}">
							<li class="selected"><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
						</c:when>
						<c:otherwise>
							<li><a href="${navigation.link}"><em>${navigation.name}</em></a></li>
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
							<li class="selected"><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
						</c:when>
						<c:otherwise>
							<li><a href="${navigation.link}"><em><fmt:message key="plugin.${navigation.name}" bundle="${globalBundle}"/></em></a></li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
		    </ul>
		</div>
		</c:if>
		
		<div id="bd">
			<div id="yui-main">
					<div class="yui-b">
						<h3>Katalog URLs</h3>
						
						<div class="yui-navset">
						    <ul class="yui-nav">
						        <li class="selected"><a href="<%=request.getContextPath()%>/catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
						        <li><a href="<%=request.getContextPath()%>/web/listWebUrls.html"><em>Web Url's</em></a></li>
						        <li><a href="<%=request.getContextPath()%>/import/importer.html"><em>Importer</em></a></li>
						    </ul>            
						</div>
						<div id="subnav">
							<ul>
								<li<c:if test="${type == 'topics'}"> class="selected"</c:if>><a href="listTopicUrls.html">Themen</a></li>
								<li<c:if test="${type == 'service'}"> class="selected"</c:if>><a href="listServiceUrls.html">Service</a></li>
								<li<c:if test="${type == 'measure'}"> class="selected"</c:if>><a href="listMeasureUrls.html">Messwerte</a></li>
							</ul>
						</div>
						
						<form:form action="createCatalogUrl.html" method="post" modelAttribute="catalogUrlCommand">
							<input type="hidden" name="type" value="${type}">
							<fieldset>
								<c:choose>
									<c:when test="${catalogUrlCommand.id > -1}">
										<legend>Katalog URL bearbeiten</legend>
									</c:when>
									<c:otherwise>
										<legend>Katalog URL anlegen</legend>										
									</c:otherwise>
								</c:choose>
								<row>
							        <label>Typ:</label>
							        <field>
							         	${type}
							        </field>
							        <desc></desc>
							    </row>
								
								<row>
							        <label>URL:</label>
							        <field>
							          <form:input path="url"/>
							            <div class="error"><form:errors path="url" /></div>
							        </field>
							        <desc></desc>
							    </row>
							
							    <row>
                                     <label>Sprache:</label>
                                     <field>
                                        <select name="metadatas" >
                                             <c:forEach var="lang" items="${langs}">
                                                 <option value="${lang.id}">${lang.metadataValue}</option>
                                             </c:forEach>
                                         </select>
                                         <div class="error"><form:errors path="metadatas" /></div>
                                     </field>
                                     <desc></desc>
                                 </row>
							
								<c:if test="${!empty metadatas['topics']}">
								<row>
							        <label>Thema:</label>
							        <field>
							        	<c:forEach items="${metadatas}" var="metadata">
							        		<c:if test="${metadata.key == 'topics'}">
							        			<c:forEach var="topic" items="${metadata.value}">
							        				<input type="checkbox" name="metadatas" value="${topic.id}" <c:if test="${fn:contains(catalogUrlCommand.metadatas , topic.metadataValue)}">checked="ckecked"</c:if>/> ${topic.metadataValue} <br/>
							        			</c:forEach>
							        		</c:if>
							        	</c:forEach>
							        </field>
							        <desc></desc>
								</row>
								</c:if>
								
								<c:if test="${!empty metadatas['funct_category']}">
								<row>
							        <label>Funkt. Kategorie:</label>
							        <field>
							        	<c:forEach items="${metadatas}" var="metadata">
							        		<c:if test="${metadata.key == 'funct_category'}">
							        			<c:forEach var="functCat" items="${metadata.value}">
							        				<input type="checkbox" name="metadatas" value="${functCat.id}" <c:if test="${fn:contains(catalogUrlCommand.metadatas , functCat.metadataValue)}">checked="ckecked"</c:if>/> ${functCat.metadataValue} <br/>
							        			</c:forEach>
							        		</c:if>
							        	</c:forEach>
							        </field>
							        <desc></desc>
								</row>
								</c:if>
								
								<c:if test="${!empty metadatas['rubric']}">
								<row>
							        <label>Rubrik:</label>
							        <field>
							        	<c:forEach items="${metadatas}" var="metadata">
							        		<c:if test="${metadata.key == 'rubric'}">
							        			<c:forEach var="rubric" items="${metadata.value}">
							        				<input type="checkbox" name="metadatas" value="${rubric.id}" <c:if test="${fn:contains(catalogUrlCommand.metadatas , rubric.metadataValue)}">checked="ckecked"</c:if>/> ${rubric.metadataValue} <br/>
							        			</c:forEach>
							        		</c:if>
							        	</c:forEach>
							        </field>
							        <desc></desc>
								</row>
								</c:if>
								
								<row>
							        <label>&nbsp;</label>
							        <field>
							            <c:set var="cancelUrl" value="listTopicUrls.html"/>
							            <c:choose>
							            	<c:when test="${type == 'service'}">
							            		<c:set var="cancelUrl" value="listServiceUrls.html"/>
							            	</c:when>
							            	<c:when test="${type == 'measure'}">
							            		<c:set var="cancelUrl" value="listMeasureUrls.html"/>
							            	</c:when>
							            </c:choose>
							            <input type="button" value="Abbrechen" onclick="window.location.href = '${cancelUrl}'"/>
							            <input type="submit" value="Weiter"/>
							        </field>
							    </row>	
							</fieldset>
							
						</form:form>
						 
					</div>
				</div>
				<div class="yui-b">
				
				</div>
			</div>
		</div>
			
		<div id="ft">
			<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
		</div>
	</div>
</body>
</html>