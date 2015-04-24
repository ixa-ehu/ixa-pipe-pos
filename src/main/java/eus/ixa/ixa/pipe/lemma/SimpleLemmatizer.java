/*
 * Copyright 2014 Rodrigo Agerri

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eus.ixa.ixa.pipe.pos.Resources;

/**
 * Lemmatize by simple dictionary lookup into a hashmap built from a file
 * containing, for each line, word\tablemma\tabpostag.
 * 
 * @author ragerri
 * @version 2014-07-08
 */
public class SimpleLemmatizer implements DictionaryLemmatizer {

  /**
   * The hashmap containing the dictionary.
   */
  private final HashMap<List<String>, String> dictMap;
  /**
   * The class dealing with loading the proper dictionary.
   */
  private final Resources tagRetriever = new Resources();
  /**
   * The language.
   */
  private final String lang;

  /**
   * Construct a hashmap from the input tab separated dictionary.
   * 
   * The input file should have, for each line, word\tablemma\tabpostag
   * 
   * @param dictionary
   *          the input dictionary via inputstream
   * @param aLang
   *          the language
   */
  public SimpleLemmatizer(final InputStream dictionary, final String aLang) {
    this.dictMap = new HashMap<List<String>, String>();
    final BufferedReader breader = new BufferedReader(new InputStreamReader(
        dictionary));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        final String[] elems = line.split("\t");
        this.dictMap.put(Arrays.asList(elems[0], elems[2]), elems[1]);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.lang = aLang;
  }

  /**
   * Get the Map containing the dictionary.
   * 
   * @return dictMap the Map
   */
  public HashMap<List<String>, String> getDictMap() {
    return this.dictMap;
  }

  /**
   * Get the dictionary keys (word and postag).
   * 
   * @param word
   *          the surface form word
   * @param postag
   *          the assigned postag
   * @return returns the dictionary keys
   */
  private List<String> getDictKeys(final String word, final String postag) {
    final String constantTag = this.tagRetriever.setTagConstant(this.lang,
        postag);
    final List<String> keys = new ArrayList<String>();
    if (postag.startsWith(String.valueOf(constantTag))) {
      keys.addAll(Arrays.asList(word, postag));
    } else {
      keys.addAll(Arrays.asList(word.toLowerCase(), postag));
    }
    return keys;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * es.ehu.si.ixa.pipe.lemmatize.DictionaryLemmatizer#lemmatize(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  public String lemmatize(final String word, final String postag) {
    String lemma = null;
    final String constantTag = this.tagRetriever.setTagConstant(this.lang,
        postag);
    final List<String> keys = this.getDictKeys(word, postag);
    // lookup lemma as value of the map
    final String keyValue = this.dictMap.get(keys);
    if (keyValue != null) {
      lemma = keyValue;
    } else if (keyValue == null
        && postag.startsWith(String.valueOf(constantTag))) {
      lemma = word;
    } else if (keyValue == null && word.toUpperCase().equals(word)) {
      lemma = word;
    } else {
      lemma = word.toLowerCase();
    }
    return lemma;
  }
}
