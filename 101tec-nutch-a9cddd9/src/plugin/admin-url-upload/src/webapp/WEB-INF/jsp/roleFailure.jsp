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
        <title><fmt:message key="login.title" bundle="${globalBundle}"/></title>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/css/reset-fonts-grids.css" />
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
        <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/element/element-min.js"></script>
        <script type="text/javascript" src="<%=request.getContextPath()%>/theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
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
                                        <h3><fmt:message key="login.headline" bundle="${globalBundle}"/></h3>
                                        <div>
                                        <div>
                                                <p>&nbsp;</p>
                                                <fmt:message key="role.failure" bundle="${globalBundle}"/>
                                        </div>
                                    </div>
                                </div>
                        </div>
                </div>
                <div id="ft">
                        <%@ include file="/WEB-INF/jsp/includes/footer.jsp" %>
                </div>
        </div>
</div>
</body>
</html>