<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head>
<title>Admin Welcome</title>
<!-- Source File -->
<link rel="stylesheet" type="text/css" href="css/yui/build/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.7.0/build/tabview/assets/skins/sam/tabview.css">
<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&2.7.0/build/element/element-min.js&2.7.0/build/tabview/tabview-min.js"></script>

</head>
<body class="yui-skin-sam">
	<div id="doc" class="yui-t1">
		<div id="hd">header</div>
			<div id="bd">
				<div id="yui-main">
					<div class="yui-b">
						<div>
							Current Pattern: ${savedPattern}
							<form action="delete.html" method="post">
								<input type="submit" value="Delete">
							</form>
						</div>
						<div id="demo" class="yui-navset">
						    <ul class="yui-nav">
						        <li class="selected"><a href="#tab1"><em>Daily</em></a></li>
						        <li><a href="#tab2"><em>Weekly</em></a></li>
						        <li><a href="#tab3"><em>Monthly</em></a></li>
						        <li><a href="#tab4"><em>Yearly</em></a></li>
						        <li><a href="#tab5"><em>Advanced</em></a></li>
						    </ul>            
						    <div class="yui-content">
						        <div id="tab1">
						        	<p>
						        		<form:form action="daily.html" method="post" commandName="clockCommand">
						        			<form:label path="hour" >Hour</form:label>
						        			<form:select path="hour">
						        				<form:options items="${hours}"/>
						        			</form:select>
						        			<form:label path="minute" >Minute</form:label>
						        			<form:select path="minute">
						        				<form:options items="${minutes}"/>
						        			</form:select>
						        			<form:label path="period" >Period</form:label>
						        			<form:select path="period">
						        				<form:options items="${periods}"/>	
						        			</form:select>
						        			<input type="submit" value="Save" />
						        		</form:form>
						        	</p>
						        </div>
						        <div id="tab2"><p>Tab Two Content</p></div>
						        <div id="tab3"><p>Tab Three Content</p></div>
						        <div id="tab4"><p>Tab Four Content</p></div>
						        <div id="tab5">
						        	<p>
						        		<form:form action="advanced.html" method="post" commandName="advancedCommand">
						        			<form:label path="pattern" >Pattern</form:label>
						        			<form:input path="pattern" />
						        			<input type="submit" value="Save" />
						        		</form:form>
						        	</p>
						        </div>
						    </div>
						</div>
						<script>
						(function() {
						    var tabView = new YAHOO.widget.TabView('demo');
						})();
						</script>
					</div>
				</div>
				<div class="yui-b">
				Welcome Navigation
					<c:forEach items="${navigations}" var="navigation">
						<a href="${navigation}">${navigation}</a>
					</c:forEach>
				</div>
			</div>
		<div id="ft">footer</div>
	</div>
</body>
</html>