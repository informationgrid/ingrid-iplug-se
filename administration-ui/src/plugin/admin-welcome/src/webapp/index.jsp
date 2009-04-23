<%
System.out.println("welcome: "+request.getContextPath());
response.sendRedirect(request.getContextPath()+"/index.html");
%>