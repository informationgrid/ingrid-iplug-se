package de.ingrid.iplug.se.urlmaintenance.persistence;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class TransactionFilter implements Filter {


  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    TransactionService transactionService = TransactionService.getInstance();
    try {
      transactionService.beginTransaction();
      chain.doFilter(request, response);
      transactionService.commitTransaction();
    } catch (Throwable t) {
      t.printStackTrace();
      transactionService.rollbackTransaction();
    } finally {
      transactionService.close();
    }
  }

    public void init(FilterConfig arg0) throws ServletException {

    }
}