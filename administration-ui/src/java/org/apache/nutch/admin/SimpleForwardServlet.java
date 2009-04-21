package org.apache.nutch.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleForwardServlet extends HttpServlet {

  private static final long serialVersionUID = -3041945936755169584L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String servletPath = req.getServletPath();
    String realPath = getServletContext().getRealPath(servletPath);
    File file = new File(realPath);
    if (!file.exists()) {
      resp.sendError(404);
      return;
    }
    FileInputStream fileInputStream = new FileInputStream(file);
    ServletOutputStream outputStream = resp.getOutputStream();
    int read = -1;
    byte[] buffer = new byte[1024];
    while ((read = fileInputStream.read(buffer)) > -1) {
      outputStream.write(buffer, 0, read);
    }
    fileInputStream.close();
    outputStream.flush();
  }
}