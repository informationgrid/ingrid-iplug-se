/**
 * 
 */
package de.ingrid.iplug.se.crawl.sns;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author marko
 */
public class DoublePadding {

  private static final DecimalFormat FORMAT = new DecimalFormat("0000000000.####");

  /**
   * @param number
   * @return the padded number as string
   */
  public static String padding(double number) {
    return FORMAT.format(number);
  }

  public static void main(String[] args) {
    System.out.println();
    System.out.println();

    Set<String> set = new TreeSet<String>();
    set.add(DoublePadding.padding(1));
    set.add(DoublePadding.padding(1.01));
    set.add(DoublePadding.padding(1.10));
    set.add(DoublePadding.padding(1.123));
    set.add(DoublePadding.padding(10.123));
    set.add(DoublePadding.padding(2));
    set.add(DoublePadding.padding(2.3));
    set.add(DoublePadding.padding(9));
    Iterator<String> iterator = set.iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next());
    }
  }
}
