<%
System.out.println("instance:"+request.getServletPath());
response.sendRedirect(request.getContextPath()+"/index.html");
%>