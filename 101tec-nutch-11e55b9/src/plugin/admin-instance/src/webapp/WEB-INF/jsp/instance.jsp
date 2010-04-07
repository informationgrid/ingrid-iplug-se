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
	<title><fmt:message key="instance.title" bundle="${localBundle}"/></title>

	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>

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
				<h3><fmt:message key="instance.headline" bundle="${localBundle}"/></h3>
				<form:form commandName="createInstance" action="index.html" method="post">
				<fieldset>
				    <legend><fmt:message key="instance.text" bundle="${localBundle}"/></legend>
				    
				    <row>
				        <label><form:label path="folderName" >Name: </form:label></label>
				        <field>
				           <form:input path="folderName"/>
				           <form:errors path="folderName" cssClass="error" element="div" />
				        </field>
				        <desc></desc>
				    </row>
				    
				     <row>
				        <label>&nbsp;</label>
				        <field>
				            <input type="submit" value="<fmt:message key="instance.submit.button" bundle="${localBundle}"/>"/>
				        </field>
				    </row>
				</fieldset>
				</form:form>
			</div> 
		</div> 
		<div class="yui-b">
			<h3><fmt:message key="instance.help.headline" bundle="${localBundle}"/></h3>
			<fmt:message key="instance.help.text" bundle="${localBundle}"/>
		</div> 
	</div>
	
	<div id="ft">
		<%@ include file="includes/footer.jsp" %>
	</div>
</div>
</body>
</html>
