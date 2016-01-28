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

package eus.ixa.ixa.pipe.pos.dict;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

/**
 * POS tagger based on Morfologik Stemming library. It requires a FSA Morfologik
 * dictionary as input. Right now this is only thought to work with monosemic
 * dictionaries.
 * 
 * @author ragerri
 * @version 2014-12-05
 * 
 */
public class MorfologikTagger implements DictionaryTagger {

  /**
   * The Morfologik steamer to perform pos tagging with FSA dictionaries.
   */
  private final IStemmer dictLookup;

  /**
   * Reads a dictionary in morfologik FSA format.
   * 
   * @param dictURL
   *          the URL containing the dictionary
   * @param aLang
   *          the language
   * @throws IOException
   *           throws an exception if dictionary path is not correct
   */
  public MorfologikTagger(final URL dictURL, final String aLang)
      throws IOException {
    this.dictLookup = new DictionaryLookup(Dictionary.read(dictURL));
  }

  /**
   * Get the postag for a surface form from a FSA morfologik generated
   * dictionary.
   * 
   * @param word
   *          the surface form
   * @return the hashmap with the word as key and the postag as value
   */
  public String tag(final String word, final String posTag) {
    final List<WordData> wdList = this.dictLookup.lookup(word.toLowerCase());
    String newPosTag = null;
    for (final WordData wd : wdList) {
      newPosTag = wd.getTag().toString();
    }
    if (newPosTag == null) {
      newPosTag = posTag;
    }
    return newPosTag;
  }
}
