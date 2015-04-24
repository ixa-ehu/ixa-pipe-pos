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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eus.ixa.ixa.pipe.pos.Resources;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

/**
 * Lemmatizer based on Morfologik Stemming library. It requires a FSA
 * Morfologik dictionary as input.
 *
 * @author ragerri
 * @version 2014-07-08
 *
 */
public class MorfologikLemmatizer implements
    eus.ixa.ixa.pipe.lemma.DictionaryLemmatizer {

  /**
   * The Morfologik steamer to perform lemmatization with FSA dictionaries.
   */
  private IStemmer dictLookup;
  /**
   * The class dealing with loading the default dictionaries.
   */
  private Resources tagRetriever = new Resources();
  /**
   * The language.
   */
  private String lang;
  /**
   * Reads a dictionary in morfologik FSA format.
   *
   * @param dictURL the URL containing the dictionary
   * @param aLang the language
   * @throws IllegalArgumentException
   * @throws IOException throws an exception if dictionary path is not correct
   */
  public MorfologikLemmatizer(final URL dictURL, String aLang)
      throws IOException {
    dictLookup = new DictionaryLookup(Dictionary.read(dictURL));
    this.lang = aLang;
  }

  /**
   * Get the lemma for a surface form word and a postag from a FSA morfologik
   * generated dictionary.
   *
   * @param word
   *          the surface form
   * @return the hashmap with the word and tag as keys and the lemma as value
   */
  private HashMap<List<String>, String> getLemmaTagsDict(final String word) {
    List<WordData> wdList = dictLookup.lookup(word);
    HashMap<List<String>, String> dictMap = new HashMap<List<String>, String>();
    for (WordData wd : wdList) {
      List<String> wordLemmaTags = new ArrayList<String>();
      wordLemmaTags.add(word);
      wordLemmaTags.add(wd.getTag().toString());
      dictMap.put(wordLemmaTags, wd.getStem().toString());
    }
    return dictMap;
  }

  /**
   * Generate the dictionary keys (word, postag).
   *
   * @param word
   *          the surface form
   * @param postag
   *          the assigned postag
   * @return a list of keys consisting of the word and its postag
   */
  private List<String> getDictKeys(final String word,
      final String postag) {
    List<String> keys = new ArrayList<String>();
    String constantTag = tagRetriever.setTagConstant(lang, postag);
    if (postag.startsWith(String.valueOf(constantTag))) {
      keys.addAll(Arrays.asList(word, postag));
    } else {
      keys.addAll(Arrays.asList(word.toLowerCase(), postag));
    }
    return keys;
  }

  /**
   * Generates the dictionary map.
   *
   * @param word
   *          the surface form word
   * @param postag
   *          the postag assigned by the pos tagger
   * @return the hash map dictionary
   */
  private HashMap<List<String>, String> getDictMap(final String word, final String postag) {
    HashMap<List<String>, String> dictMap = new HashMap<List<String>, String>();
    String constantTag = tagRetriever.setTagConstant(lang, postag);
    if (postag.startsWith(String.valueOf(constantTag))) {
      dictMap = this.getLemmaTagsDict(word);
    } else {
      dictMap = this.getLemmaTagsDict(word.toLowerCase());
    }
    return dictMap;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * es.ehu.si.ixa.pipe.lemmatize.DictionaryLemmatizer#lemmatize(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  public String lemmatize(final String word,
      final String postag) {
    String lemma = null;
    List<String> keys = this.getDictKeys(word, postag);
    HashMap<List<String>, String> dictMap = this.getDictMap(word, postag);
    // lookup lemma as value of the map
    String keyValue = dictMap.get(keys);
    String constantTag = tagRetriever.setTagConstant(lang, postag);
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
