<%
System.out.println("instance: "+request.getContextPath());
response.sendRedirect(request.getContextPath()+"/index.html");
%>