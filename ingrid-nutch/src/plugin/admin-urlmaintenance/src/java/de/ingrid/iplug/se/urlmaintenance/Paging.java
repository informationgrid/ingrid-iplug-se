package de.ingrid.iplug.se.urlmaintenance;

import java.util.ArrayList;
import java.util.List;

public class Paging {

  private int _pagesToShow;

  private int _hitsPerPage;

  private int _totalHits;

  private int _currentPage;

  private List<Page> _pages = new ArrayList<Page>();

  public Paging(int pagesToShow, int hitsPerPage, int totalHits, int page) {
    _totalHits = totalHits;
    _hitsPerPage = hitsPerPage;
    _pagesToShow = pagesToShow;
    _currentPage = page;
    createPages(_pages, pagesToShow, hitsPerPage, totalHits);
  }

  private void createPages(List<Page> pages, int pagesToShow, int hitsPerPage,
      int totalHits) {
    int maxPage = getLastPage(totalHits, hitsPerPage);
    _currentPage = Math.min(_currentPage, maxPage);
    int pageBorder = (_currentPage % pagesToShow);
    int minPageToShow = _currentPage - pageBorder;
    if ((pagesToShow % 2) == 0) {
      minPageToShow--;
    }
    minPageToShow = Math.max(1, minPageToShow);
    int maxPageToShow = Math.min(maxPage, minPageToShow + pagesToShow);

    if (_currentPage >= _pagesToShow) {
      pages.add(new Page("<<", minPageToShow, false));
    } else {
      minPageToShow = 0;
    }
    int pagesInThisSection = Math.min(pagesToShow, maxPageToShow
        - minPageToShow);
    for (int i = 1; (i <= pagesInThisSection); i++) {
      int pageNumber = minPageToShow + i;
      pages.add(new Page(pageNumber + "", pageNumber,
          pageNumber == _currentPage));
    }
    if (maxPageToShow < maxPage) {
      if (minPageToShow == 0) {
        minPageToShow = 1;
      } else {
        minPageToShow++;
      }
      pages.add(new Page(">>", minPageToShow + pagesToShow, false));
    }
  }

  public boolean isCurrentPage(Page page) {
    return page.getPage() == _currentPage;
  }

  public int getTotalHits() {
    return _totalHits;
  }

  public int getCurrentPage() {
    return _currentPage;
  }

  public List<Page> getPages() {
    return _pages;
  }

  public int getHitsPerPage() {
    return _hitsPerPage;
  }

  public static class Page {

    private int _page;

    private String _label;

    private boolean _isCurrentPage;

    public Page(String label, int page, boolean isCurentPage) {
      _label = label;
      _page = page;
      _isCurrentPage = isCurentPage;
    }

    public int getPage() {
      return _page;
    }

    public String getLabel() {
      return _label;
    }

    public boolean isCurrentPage() {
      return _isCurrentPage;
    }

  }

  public static int getStart(int page, int hitsPerPage) {
    return (page - 1) * hitsPerPage;
  }

  public static int getLastPage(int maxCount, int hitsPerPage) {
    int maxPages = maxCount / hitsPerPage;
    int rest = maxCount % hitsPerPage;
    if (rest > 0) {
      maxPages++;
    }
    if (maxPages == 0) {
      maxPages = 0;
    }

    return maxPages;
  }
}
