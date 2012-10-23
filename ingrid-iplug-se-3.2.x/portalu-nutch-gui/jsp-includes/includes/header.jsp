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

<div style="text-align:right;float:right;margin:10px">
<%
java.security.Principal  principal = request.getUserPrincipal();

String pluginBasePath = ".";
String[] rootPathElements = rootPath.split("/");
int rootPathElementCount = rootPathElements.length;
while (rootPathElementCount > 1) {
  pluginBasePath += "/..";
  rootPathElementCount--;
}


if(principal != null) {
%>
	<a href ="<%=pluginBasePath%>/auth/logout.html" style="color:black">Logout</a>
<%
}
%>
</div>
<img src="<%=rootPath%>/theme/${theme}/gfx/logo.gif" />
<div class="float_left" style="color: white; font-size: 150%; display: inline-block;">${title}</div>