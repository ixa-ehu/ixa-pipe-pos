/* 
 *Copyright 2015 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eus.ixa.ixa.pipe.lemma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import opennlp.tools.util.Span;
import eus.ixa.ixa.pipe.pos.StringUtils;

/**
 * Reads a dictionary multiword\tmultiwordlemma\tpostag\tambiguity and
 * matches the multiwords for each sentence.
 * @author ragerri
 * @version 2015-01-28
 *
 */
public class MultiWordMatcher {

  private static final Pattern tabPattern = Pattern.compile("\t");
  private static final Pattern linePattern = Pattern.compile("#");
  private static Map<String, String> dictionary;
  
  /**
   * Construct a multiword matcher with a dictionary for a given language.
   * @param props
   * @throws IOException
   */
  public MultiWordMatcher(Properties props) throws IOException {
    if (dictionary == null) {
      loadDictionary(props);
    }
  }
  
  /**
   * Load the dictionaries.
   * @param props the properties object
   * @throws IOException if io problems
   */
  private void loadDictionary(Properties props) throws IOException {
    dictionary = new HashMap<String, String>();
    String lang = props.getProperty("language");
    InputStream dictInputStream = getMultiWordDict(lang);
    if (dictInputStream == null) {
      System.err.println("ERROR: Not multiword dictionary for language " + lang + " in src/main/resources!!");
      System.exit(1);
    }
    BufferedReader breader = new BufferedReader(new InputStreamReader(dictInputStream, Charset.forName("UTF-8")));
    String line;
    while ((line = breader.readLine()) != null) {
      String[] lineArray = tabPattern.split(line);
      if (lineArray.length == 4) {
        Matcher lineMatcher = linePattern.matcher(lineArray[0].toLowerCase());
        dictionary.put(lineMatcher.replaceAll(" "), lineArray[2]);
      } else {
        System.err.println("WARNING: line starting with " + lineArray[0] + " is not well-formed; skipping!!");
      }
    }
  }
  
  /**
   * Get the dictionary for the {@code MultiWordMatcher}.
   *
   * @param lang
   *          the language
   * @return the inputstream of the dictionary
   */
  private final InputStream getMultiWordDict(final String lang) {
    InputStream dict = null;
    //TODO complete locutions dictionary and binarize
    if (lang.equalsIgnoreCase("es")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/freeling/es-locutions.txt");
    }
    if (lang.equalsIgnoreCase("gl")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/ctag/gl-locutions.txt");
    }
    return dict;
  }
  
  /**
   * Get input text and join the multiwords found in the dictionary object.
   * @param tokens the input text
   * @return the output text with the joined multiwords
   */
  public final String[] getTokensWithMultiWords(String[] tokens) {
    Span[] multiWordSpans = multiWordsToSpans(tokens);
    List<String> tokenList = new ArrayList<String>(Arrays.asList(tokens));
    int counter = 0;
    for (Span mwSpan : multiWordSpans) {
      int fromIndex = mwSpan.getStart() - counter;
      int toIndex = mwSpan.getEnd() - counter;
      //System.err.println(fromIndex + " " + toIndex);
      //add to the counter the length of the sublist removed
      //to allow the fromIndex and toIndex to match wrt to the tokenList indexes
      counter = counter + tokenList.subList(fromIndex, toIndex).size() - 1;
      //create the multiword joining the sublist
      String multiWord = Joiner.on("#").join(tokenList.subList(fromIndex, toIndex));
      //remove the sublist containing the tokens to be replaced in the span
      tokenList.subList(fromIndex, toIndex).clear();
      //add the multiword containing the tokens in one Span
      tokenList.add(fromIndex, multiWord);
    }
    return tokenList.toArray(new String[tokenList.size()]);
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
