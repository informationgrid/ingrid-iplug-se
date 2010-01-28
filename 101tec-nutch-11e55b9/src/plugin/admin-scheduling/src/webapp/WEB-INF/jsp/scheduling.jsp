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
<%@ include file="/WEB-INF/jsp/includes/include.jsp" %>
<html>
<head>
	<title><fmt:message key="scheduling.title" bundle="${localBundle}"/></title>

	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/button-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/button/assets/skins/sam/button.css"> 
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
					<h3><fmt:message key="scheduling.headline" bundle="${localBundle}"/></h3>
					<div class="row">
						<form action="delete.html" method="post">
							<label><fmt:message key="scheduling.cronPattern" bundle="${localBundle}"/>:</label>&nbsp;${savedPattern}
							<input type="image" src="<%=request.getContextPath()%>/theme/${theme}/gfx/delete.png" title="Löschen" align="absmiddle">
						</form>
					</div>
					<div class="row">
						<label><fmt:message key="scheduling.crawlParams" bundle="${localBundle}"/>:</label>&nbsp;${savedCrawlData}
					</div>
					
					<div style="margin-top:25px"></div>
					
					<div id="schedulingTabs" class="yui-navset">
					    <ul class="yui-nav">
					        <li class="selected"><a href="#tab1"><em><fmt:message key="scheduling.day" bundle="${localBundle}"/></em></a></li>
					        <li><a href="#tab2"><em><fmt:message key="scheduling.week" bundle="${localBundle}"/></em></a></li>
					        <li><a href="#tab3"><em><fmt:message key="scheduling.month" bundle="${localBundle}"/></em></a></li>
					        <li><a href="#tab4"><em><fmt:message key="scheduling.advanced" bundle="${localBundle}"/></em></a></li>
					    </ul>            
					    <div class="yui-content">
					        <div id="tab1">
					        	<p>
					        		<form:form action="daily.html" method="post" commandName="clockCommand">
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.daily" bundle="${localBundle}"/></legend>
						        			
						        			<row>
										        <label><fmt:message key="scheduling.hour" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="hour">
								        				<form:options items="${hours}"/>
								        			</form:select>
										            <div class="error"><form:errors path="hour" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.minute" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="minute">
								        				<form:options items="${minutes}"/>
								        			</form:select>
										            <div class="error"><form:errors path="minute" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.daytime" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="period">
								        				<form:options items="${periods}"/>	
								        			</form:select>
										            <div class="error"><form:errors path="period" /></div>
										        </field>
										        <desc></desc>
										    </row>
					        			</fieldset>
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.crawlParams" bundle="${localBundle}"/></legend>
					        				
					        				<row>
										        <label><fmt:message key="scheduling.crawlDepth" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="depth">
								        				<form:options items="${depths}"/>
								        			</form:select>
										            <div class="error"><form:errors path="depth" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.pagesPerSegment" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:input path="topn" />
										            <div class="error"><form:errors path="topn" /></div>
										        </field>
										        <desc></desc>
										    </row>					        				
					        			</fieldset>
					        			
					        			<fieldset>
					        				<label>&nbsp;</label>
									        <field>
									           	<input type="submit" value="<fmt:message key="button.save" bundle="${globalBundle}"/>" />
									        </field>
									        <desc></desc>	
					        			</fieldset>
					        			
					        		</form:form>
					        	</p>
					        </div>
					        <div id="tab2">
					        	<p>
					        	
					        		<form:form action="weekly.html" method="post" commandName="weeklyCommand">
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.weekly" bundle="${localBundle}"/></legend>
						        			
						        			<row>
										        <label><fmt:message key="scheduling.hour" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="hour">
								        				<form:options items="${hours}"/>
								        			</form:select>
										            <div class="error"><form:errors path="hour" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.minute" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="minute">
								        				<form:options items="${minutes}"/>
								        			</form:select>
										            <div class="error"><form:errors path="minute" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.daytime" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="period">
								        				<form:options items="${periods}"/>	
								        			</form:select>
										            <div class="error"><form:errors path="period" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.weekdays" bundle="${localBundle}"/>:</label>
										        <field>
										           	<c:forEach var="day" items="${days}">
										           		<div id="dayOfWeekButtons" style="float:left; margin:2px 2px 0 0 ;">
										           			<input type="checkbox" name="days" value="${day}" id="dayOfWeekButtons_${day}" />
										           		</div>
										           	</c:forEach>
										            <div class="error"><form:errors path="days" /></div>
										        </field>
										        <desc></desc>
										    </row>
					        			</fieldset>
					        			
					        			<script type="text/javascript">
											(function () {
									    	var Button = YAHOO.widget.Button;
			
			
									    	YAHOO.util.Event.onContentReady("dayOfWeekButtons", function () {
									    		<c:forEach items="${days}" var="day">
										            var oCheckButton_${day} = new Button("dayOfWeekButtons_${day}", { label:"${day}"});
									            </c:forEach>
									        });
			
										  }());
										</script>
					        			
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.crawlParams" bundle="${localBundle}"/></legend>
					        				
					        				<row>
										        <label><fmt:message key="scheduling.crawlDepth" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="depth">
								        				<form:options items="${depths}"/>
								        			</form:select>
										            <div class="error"><form:errors path="depth" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.pagesPerSegment" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:input path="topn" />
										            <div class="error"><form:errors path="topn" /></div>
										        </field>
										        <desc></desc>
										    </row>					        				
					        			</fieldset>
					        			
					        			<fieldset>
					        				<label>&nbsp;</label>
									        <field>
									           	<input type="submit" value="<fmt:message key="button.save" bundle="${globalBundle}"/>" />
									        </field>
									        <desc></desc>	
					        			</fieldset>
					        		</form:form>
					        		
					        	</p>
					        </div>
					        <div id="tab3">
					        	<p>
					        	
									
		
									<form:form action="monthly.html" method="post" commandName="monthlyCommand">
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.monthly" bundle="${localBundle}"/></legend>
						        			
						        			<row>
										        <label><fmt:message key="scheduling.hour" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="hour">
								        				<form:options items="${hours}"/>
								        			</form:select>
										            <div class="error"><form:errors path="hour" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.minute" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="minute">
								        				<form:options items="${minutes}"/>
								        			</form:select>
										            <div class="error"><form:errors path="minute" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.daytime" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="period">
								        				<form:options items="${periods}"/>	
								        			</form:select>
										            <div class="error"><form:errors path="period" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.atDays" bundle="${localBundle}"/>:</label>
										        <field>
										           	<c:forEach items="${month}" var="dayOfMonth">
		     											<div id="checkboxButtons" style="float:left; margin:2px 2px 0 0 ">
		     											<input name="daysOfMonth" type="checkbox" id="daysOfMonth_${dayOfMonth}" value="${dayOfMonth}"> 
		     											</div>
		     										</c:forEach>
										            <div class="error"><form:errors path="daysOfMonth" /></div>
										        </field>
										        <desc></desc>
										    </row>
					        			</fieldset>
					        			
					        			<script type="text/javascript">
										(function () {
									    	var Button = YAHOO.widget.Button;
			
			
									    	YAHOO.util.Event.onContentReady("checkboxButtons", function () {
									    		<c:forEach items="${month}" var="dayOfMonth">
										            var oCheckButton_${dayOfMonth} = new Button("daysOfMonth_${dayOfMonth}", { label:"<fmt:formatNumber value="${dayOfMonth+1}" pattern="00"/>"});
									            </c:forEach>
									        });
			
										  }());
										</script>
					        			
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.crawlParams" bundle="${localBundle}"/></legend>
					        				
					        				<row>
										        <label><fmt:message key="scheduling.crawlDepth" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="depth">
								        				<form:options items="${depths}"/>
								        			</form:select>
										            <div class="error"><form:errors path="depth" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.pagesPerSegment" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:input path="topn" />
										            <div class="error"><form:errors path="topn" /></div>
										        </field>
										        <desc></desc>
										    </row>					        				
					        			</fieldset>
					        			
					        			<fieldset>
					        				<label>&nbsp;</label>
									        <field>
									           	<input type="submit" value="<fmt:message key="button.save" bundle="${globalBundle}"/>" />
									        </field>
									        <desc></desc>	
					        			</fieldset>
									
									</form:form>																        		
					        	</p>
					        </div>
					        <div id="tab4">
					        	<p>
						        	<form:form action="advanced.html" method="post" commandName="advancedCommand">
						        		<fieldset>
					        				<legend><fmt:message key="scheduling.advanced" bundle="${localBundle}"/></legend>
					        				
					        				<row>
					        					<label><fmt:message key="scheduling.cronPattern" bundle="${localBundle}"/>:</label>
					        					<field>
					        						<form:input path="pattern" />
					        						<div class="error"><form:errors path="pattern" /></div>
					        						<div><fmt:message key="scheduling.cronPatternDescription"/></div>
					        					</field>
					        				</row>
					        				<row>
					        					<desc><fmt:message key="scheduling.cronPatternExample"/></desc>
					        				</row>
						        		</fieldset>
					        			
					        			<fieldset>
					        				<legend><fmt:message key="scheduling.crawlParams" bundle="${localBundle}"/></legend>
					        				
					        				<row>
										        <label><fmt:message key="scheduling.crawlDepth" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:select path="depth">
								        				<form:options items="${depths}"/>
								        			</form:select>
										            <div class="error"><form:errors path="depth" /></div>
										        </field>
										        <desc></desc>
										    </row>
										    
										    <row>
										        <label><fmt:message key="scheduling.pagesPerSegment" bundle="${localBundle}"/>:</label>
										        <field>
										           	<form:input path="topn" />
										            <div class="error"><form:errors path="topn" /></div>
										        </field>
										        <desc></desc>
										    </row>					        				
					        			</fieldset>
					        			
					        			<fieldset>
					        				<label>&nbsp;</label>
									        <field>
									           	<input type="submit" value="<fmt:message key="button.save" bundle="${globalBundle}"/>" />
									        </field>
									        <desc></desc>	
					        			</fieldset>
					        		</form:form>
					        	</p>
					        </div>
					    </div>
					</div>
					<script  type="text/javascript">
					(function() {
					    var tabView = new YAHOO.widget.TabView('schedulingTabs');
					})();
					</script>
					
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
