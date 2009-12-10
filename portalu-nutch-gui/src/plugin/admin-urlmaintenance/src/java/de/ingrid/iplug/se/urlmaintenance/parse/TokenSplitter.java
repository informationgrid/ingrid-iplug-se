package de.ingrid.iplug.se.urlmaintenance.parse;

import java.util.LinkedList;

public class TokenSplitter {
  private static final char ESCAPE_CHARACTER = '\"';

  private static final char SPLIT_CHARACTER = ',';

  public static String[] split(final String line) {
    final LinkedList<String> tokens = new LinkedList<String>();
    int lastSplitChar = 0;
    int nextSplit;
    while ((nextSplit = line.indexOf(SPLIT_CHARACTER, lastSplitChar)) != -1) {
      String token = line.substring(lastSplitChar, nextSplit);// normal token
      // respect escape tokens ie. "token"
      if (token.length() > 0 && token.charAt(0) == ESCAPE_CHARACTER) {
        int indexOfTokenEnd = line.indexOf(ESCAPE_CHARACTER + "" + SPLIT_CHARACTER, lastSplitChar + 1);
        if (indexOfTokenEnd == -1) {
          indexOfTokenEnd = line.indexOf(ESCAPE_CHARACTER, lastSplitChar + 1);// last
          // token
          if (indexOfTokenEnd < 0) {
            return new String[0];
          }
          assert indexOfTokenEnd + 1 == line.length();
        }
        token = line.substring(lastSplitChar + 1, indexOfTokenEnd);
        lastSplitChar += token.length() + 3;
      } else {
        lastSplitChar += token.length() + 1;
      }
      tokens.add(token);
    }

    // respect last token
    if (lastSplitChar < line.length()) {
      String lastToken = line.substring(lastSplitChar);
      if (lastToken.startsWith("" + ESCAPE_CHARACTER) && lastToken.endsWith("" + ESCAPE_CHARACTER)) {
        lastToken = lastToken.substring(1, lastToken.length() - 1);
      }
      tokens.add(lastToken);
    }

    removeEmptyLastTokens(tokens);
    return tokens.toArray(new String[tokens.size()]);
  }

  private static void removeEmptyLastTokens(final LinkedList<String> tokens) {
    // removes all empty tokens bit only the last ones.
    // the massimporter needs that behaviour
    while (tokens.size() > 0 && tokens.getLast().equals("")) {
      tokens.removeLast();
    }
  }
}
