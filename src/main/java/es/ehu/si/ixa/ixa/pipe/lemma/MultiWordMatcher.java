package es.ehu.si.ixa.ixa.pipe.lemma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.si.ixa.ixa.pipe.pos.StringUtils;

import opennlp.tools.util.Span;

public class MultiWordMatcher {

  private static final Pattern tabPattern = Pattern.compile("\t");
  private static final Pattern linePattern = Pattern.compile("_");
  private Map<String, String> dictionary = new HashMap<String, String>();
  
  public MultiWordMatcher(InputStream in) throws IOException {

    BufferedReader breader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
    String line;
    while ((line = breader.readLine()) != null) {
      String[] lineArray = tabPattern.split(line);
      if (lineArray.length == 4) {
        Matcher lineMatcher = linePattern.matcher(lineArray[0].toLowerCase());
        dictionary.put(lineMatcher.replaceAll(" "), lineArray[2].toLowerCase());
      }
    }
  }
  
  /**
   * Detects multiword expressions ignoring case.
   * 
   * @param tokens
   *          the tokenized sentence
   * @return spans of the multiword
   */
  public final Span[] multiWordsToSpans(final String[] tokens) {
    List<Span> multiWordsFound = new LinkedList<Span>();

    for (int offsetFrom = 0; offsetFrom < tokens.length; offsetFrom++) {
      Span multiwordFound = null;
      String tokensSearching[] = new String[] {};
      
      for (int offsetTo = offsetFrom; offsetTo < tokens.length; offsetTo++) {

        int lengthSearching = offsetTo - offsetFrom + 1;
        if (lengthSearching > getMaxTokenCount()) {
          break;
        } else {
          tokensSearching = new String[lengthSearching];
          System.arraycopy(tokens, offsetFrom, tokensSearching, 0,
              lengthSearching);

          String entryForSearch = StringUtils.getStringFromTokens(
              tokensSearching);
          String entryValue = dictionary.get(entryForSearch.toLowerCase());
          if (entryValue != null) {
            multiwordFound = new Span(offsetFrom, offsetTo + 1, entryValue);
          }
        }
      }
      if (multiwordFound != null) {
        multiWordsFound.add(multiwordFound);
        // skip over the found tokens for the next search
        offsetFrom += (multiwordFound.length() - 1);
      }
    }
    return multiWordsFound.toArray(new Span[multiWordsFound.size()]);
  }
  
  /**
   * Get the <key,value> size of the dictionary.
   * @return maximum token count in the dictionary
   */
  public int getMaxTokenCount() {
    return dictionary.size();
  }
}
