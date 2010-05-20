<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="../includes/include.jsp" %>
<html>
<head>
	<title>Admin URL Pflege - Limit URLs</title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/fonts.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
</head>

<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
	<div id="doc2" class="yui-t4">
		<div id="hd">
			<%@ include file="../includes/header.jsp" %>
		</div>
		
		<%@ include file="../includes/menu.jsp" %>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3>Web URLs</h3>
					
					<div class="yui-navset">
					    <ul class="yui-nav">
					        <li><a href="../catalog/listTopicUrls.html"><em>Katalog Url's</em></a></li>
					        <li class="selected"><a href="../web/listWebUrls.html"><em>Web Url's</em></a></li>
					        <li><a href="../import/importer.html"><em>Importer</em></a></li>
					    </ul>            
					</div>
					
					<c:set var="maxLimitUrls" value="${fn:length(startUrlCommand.limitUrlCommands)}"/>
					<c:set var="limitUrlCounter" value="-1"/>
					<c:forEach items="${startUrlCommand.limitUrlCommands}" var="limitUrl">
						<c:set var="limitUrlCounter" value="${limitUrlCounter+1}"/>
						<c:choose>
							<c:when test="${limitUrlCounter < maxLimitUrls-1}">
								<div class="row">
								<form:form action="removeLimitUrl.html" method="post" modelAttribute="startUrlCommand">
									${startUrlCommand.limitUrlCommands[limitUrlCounter].url}
									(<c:forEach var="metadata" items="${startUrlCommand.limitUrlCommands[limitUrlCounter].metadatas}">
										<fmt:message key="${metadata.metadataKey}.${metadata.metadataValue}" />
									</c:forEach>)
									<input type="hidden" name="index" value="${limitUrlCounter}" />
									<input type="image" src="../theme/${theme}/gfx/delete.png" align="absmiddle" title="L�schen"/>
								</form:form>
								</div>
							</c:when>
							<c:otherwise>
								<form:form action="addLimitUrl.html" method="post" modelAttribute="startUrlCommand">
									<fieldset>
										<c:choose>
											<c:when test="${startUrlCommand.id > -1}">
												<legend>Web Url bearbeiten - Limit Url</legend>
											</c:when>
											<c:otherwise>
												<legend>Web Url anlegen - Limit Url</legend>										
											</c:otherwise>
										</c:choose>
										<row>
									        <label>Limit URL:</label>
									        <field>
									           <form:input path="limitUrlCommands[${limitUrlCounter}].url"/>
									            <form:errors path="limitUrlCommands[${limitUrlCounter}].url" cssClass="error" element="div"/>
									        </field>
									        <desc></desc>
									    </row>
									    
									    <row>
									        <label>Sprache:</label>
									        <field>
									           <select name="limitUrlCommands[${limitUrlCounter}].metadatas" >
													<c:forEach var="lang" items="${langs}">
														<option value="${lang.id}"><fmt:message key="${lang.metadataKey}.${lang.metadataValue}" /></option>
													</c:forEach>
												</select>
									        </field>
									        <desc></desc>
									    </row>
									    
									    <row>
									        <label>Typ:</label>
									        <field>
                                                <c:forEach var="type" items="${datatypes}">
					                                <c:if test="${type.metadataValue != 'topics' && type.metadataValue != 'service' && type.metadataValue != 'measure'}">
					                                    <input type="checkbox" name="limitUrlCommands[${limitUrlCounter}].metadatas" value="${type.id}" /><fmt:message key="${type.metadataKey}.${type.metadataValue}" /><br/>
					                                </c:if>
                                                </c:forEach>
                                            </field>
									        <desc></desc>
									    </row>
									    
									    <row>
		                                    <form:errors path="limitUrlCommands[${limitUrlCounter}].provider" cssClass="error" element="div"/>
		                                    <form:errors path="limitUrlCommands[${limitUrlCounter}].metadatas" cssClass="error" element="div"/>
		                                </row>
									    
									    <row>
									        <label>&nbsp;</label>
									        <field>
									            <input type="submit" value="Hinzuf�gen"/>
									            <c:if test="${maxLimitUrls > 1}">
										            <input type="button" value="Weiter" onclick="window.location.href='addExcludeUrl.html'" />
										        </c:if>
									        </field>
									    </row>
									    
								    </fieldset>
									
								</form:form>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					
					
				</div>
				
				<div class="yui-b">
				
				</div>
			</div>
		</div>
		
		<div id="ft">
			<%@ include file="../includes/footer.jsp" %>
		</div>
		
	</div>
</body>
</html>