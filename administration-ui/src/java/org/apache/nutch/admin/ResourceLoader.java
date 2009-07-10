package org.apache.nutch.admin;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceLoader extends HttpServlet {

  public static final Log LOG = LogFactory.getLog(SimpleForwardServlet.class);

  private static final long serialVersionUID = -3041945936755169584L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // e.g. /test.css
    String servletPath = req.getServletPath();
    String webappResource = "/webapp" + servletPath;
    InputStream inputStream = ResourceLoader.class
        .getResourceAsStream(webappResource);
    if (inputStream == null) {
      LOG.warn("resource not found in classpath: " + webappResource);
      resp.sendError(404);
      return;
    }
    ServletOutputStream outputStream = resp.getOutputStream();
    int read = -1;
    byte[] buffer = new byte[1024];
    while ((read = inputStream.read(buffer)) > -1) {
      outputStream.write(buffer, 0, read);
    }
    inputStream.close();
    outputStream.flush();
  }
}