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
	<title><fmt:message key="crawlStatistics.title" bundle="${localBundle}"/></title>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css">
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/json/json-min.js"></script> 
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/charts/charts-min.js"></script> 
	<script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/datasource/datasource-min.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
	<div id="doc2">
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
					<h3><fmt:message key="crawlStatistics.crawlDbStatsTotal" bundle="${localBundle}"/></h3>
					<div id="chart" style="height:${(fn:length(crawldbStatistic)) * 100}px">Unable to load Flash content.</div>

					<script type="text/javascript">
					
						YAHOO.widget.Chart.SWFURL = "<%=request.getContextPath()%>/theme/${theme}/js/yui/build/charts/assets/charts.swf";
						
						//used to format x axis labels
						YAHOO.example.numberToCurrency = function( value )
						{
							return YAHOO.util.Number.format(Number(value), {thousandsSeparator: "."});
						}
					
						//manipulating the DOM causes problems in ie, so create after window fires "load"
						YAHOO.util.Event.addListener(window, "load", function()
						{
					
						//--- data
					
							YAHOO.example.stats =
							[
							<c:forEach items="${crawldbStatistic}" var="statistic" begin="0">
								{ host: '${statistic.host}', crawlDbCount: ${statistic.overallCount}, segmentCount: ${statistic.fetchSuccessCount} },
							</c:forEach>	
							];
					
							var statsData = new YAHOO.util.DataSource( YAHOO.example.stats );
							statsData.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
							statsData.responseSchema = { fields: [ "host", "crawlDbCount", "segmentCount" ] };
					
						//--- chart
					
							var seriesDef =
							[
								{
									xField: "crawlDbCount",
									displayName: "<fmt:message key="crawlStatistics.urlsTotal" bundle="${localBundle}"/>"
								},
								{
									xField: "segmentCount",
									displayName: "<fmt:message key="crawlStatistics.urlsFetched" bundle="${localBundle}"/>"
								},
								
							];
					
							var numberAxis = new YAHOO.widget.NumericAxis();
							//currencyAxis.labelFunction = "YAHOO.example.numberToCurrency";
					
							var mychart = new YAHOO.widget.BarChart( "chart", statsData,
							{
								series: seriesDef,
								yField: "host",
								xAxis: numberAxis,
								style: {legend:{display: "top"}}
							});
					
						
						});
					
					</script>
					
					<div style="margin-top:25px"></div>
					<h3><fmt:message key="crawlStatistics.segmentStats" bundle="${localBundle}"/></h3>
					
					<div id="chart2" style="height:${(fn:length(shardStatistic)) * 100}px">Unable to load Flash content.</div>

					<script type="text/javascript">
					
						YAHOO.widget.Chart.SWFURL = "<%=request.getContextPath()%>/theme/${theme}/js/yui/build/charts/assets/charts.swf";
						
						//used to format x axis labels
						YAHOO.example.numberToCurrency = function( value )
						{
							return YAHOO.util.Number.format(Number(value), {thousandsSeparator: "."});
						}
					
						//manipulating the DOM causes problems in ie, so create after window fires "load"
						YAHOO.util.Event.addListener(window, "load", function()
						{
					
						//--- data
					
							YAHOO.example.stats2 =
							[
							<c:forEach items="${shardStatistic}" var="statistic" begin="0">
								{ host: '${statistic.host}', crawlDbCount: ${statistic.overallCount}, segmentCount: ${statistic.fetchSuccessCount} },
								
							</c:forEach>	
							];
					
							var statsData2 = new YAHOO.util.DataSource( YAHOO.example.stats2);
							statsData2.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
							statsData2.responseSchema = { fields: [ "host", "crawlDbCount", "segmentCount" ] };
					
						//--- chart
					
							var seriesDef =
							[
								{
									xField: "crawlDbCount",
									displayName: "<fmt:message key="crawlStatistics.urlsTotal" bundle="${localBundle}"/>"
								},
								{
									xField: "segmentCount",
									displayName: "<fmt:message key="crawlStatistics.urlsFetched" bundle="${localBundle}"/>"
								},
								
							];
					
							var numberAxis = new YAHOO.widget.NumericAxis();
							//currencyAxis.labelFunction = "YAHOO.example.numberToCurrency";
					
							var mychart = new YAHOO.widget.BarChart( "chart2", statsData2,
							{
								series: seriesDef,
								yField: "host",
								xAxis: numberAxis,
								style: {legend:{display: "top"}}
							});
					
						
						});
					
					</script>

				</div>	
		</div>
	</div>
	<div id="ft">
		<%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
