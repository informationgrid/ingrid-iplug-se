package de.ingrid.iplug.se.urlmaintenance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CatchErrorController {

  @RequestMapping(value = "/error.html", method = RequestMethod.GET)
  public String catchError(HttpServletRequest request, Model model) {
    System.out.println("CatchErrorController.catchError()");
    Throwable ex = (Throwable) request
        .getAttribute("javax.servlet.error.exception");
    if (ex != null) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(outputStream);
      ex.printStackTrace(printStream);
      printStream.flush();
      byte[] byteArray = outputStream.toByteArray();
      model.addAttribute("message", new String(byteArray));
      printStream.close();
    }
    return "error";
  }
}
