<div style="text-align:right;float:right;margin:10px">
<%
java.security.Principal  principal = request.getUserPrincipal();
if(principal != null) {
%>
	<a href ="<%=request.getContextPath()%>/auth/logout.html" style="color:black">Logout</a>
<%
}
%>
</div>
<img src="<%=request.getContextPath()%>/theme/${theme}/gfx/logo.gif" />