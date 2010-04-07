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
	<title><fmt:message key="login.title" bundle="${globalBundle}"/></title>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/reset-fonts-grids.css" />
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/js/yui/build/tabview/assets/skins/sam/tabview.css" />
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/element/element-min.js"></script>
	<script type="text/javascript" src="../theme/${theme}/js/yui/build/tabview/tabview-min.js"></script>
	<link rel="stylesheet" type="text/css" href="../theme/${theme}/css/style.css" />
</head>
<body class="yui-skin-sam">
    <% rootPath = "../.."; %> 
	<div id="doc2">					
		<div id="hd">
			<%@ include file="includes/header.jsp" %>
		</div>
		
		<%@ include file="includes/menu.jsp" %>
		
		<div id="bd">
			<div id="yui-main">
				<div class="yui-b">
					<h3><fmt:message key="login.headline" bundle="${globalBundle}"/></h3>
					<div>
				        <div>
				        	<p>&nbsp;</p>
				            <form method="post" action="j_security_check" id="login">
				                <fieldset>
				                  <legend><fmt:message key="login.legend" bundle="${globalBundle}"/></legend>
				                  	<row>
					                    <label><fmt:message key="login.username" bundle="${globalBundle}"/></label>
					                    <field>
						                    <input type="text" name="j_username"/>
					                    </field>
				                    </row>
				                    <row>
				                    	<label><fmt:message key="login.password" bundle="${globalBundle}"/></label>
				                    	<field>
				                    		<input type="password" name="j_password" />
				                    	</field>
				                    </row>
				                </fieldset>
				                  <input type="submit" value="Login"/>
				            </form>
				        	<c:if test="${!securityEnabled}">
					        	<script>
					        		document.getElementById('login').submit();
					        	</script>
				        	</c:if>
				        </div>
				    </div>
				</div>
			</div>
		</div>		
		<div id="ft">
			<%@ include file="includes/footer.jsp" %>
		</div>
	</div>
</div>
</body>
</html>

