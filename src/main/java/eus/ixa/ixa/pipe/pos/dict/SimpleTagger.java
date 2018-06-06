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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * POS tag by simple dictionary lookup into a hashmap built from a file
 * containing, for each line, word\tablemma\tabpostag. This is originally
 * thought to work with monosemic dictionaries.
 * 
 * @author ragerri
 * @version 2014-12-05
 */
public class SimpleTagger implements DictionaryTagger {

  /**
   * The hashmap containing the dictionary.
   */
  private final HashMap<String, String> dictMap;

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
  public SimpleTagger(final InputStream dictionary, final String aLang) {
    this.dictMap = new HashMap<String, String>();
    final BufferedReader breader = new BufferedReader(new InputStreamReader(
        dictionary));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        final String[] elems = line.split("\t");
        this.dictMap.put(elems[0], elems[2]);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see es.ehu.si.ixa.ixa.pipe.pos.dict.DictionaryTagger#tag(java.lang.String,
   * java.lang.String)
   */
  public String tag(final String word, final String postag) {
    // lookup postag as value of the map
    String newPosTag = this.dictMap.get(word.toLowerCase());
    if (newPosTag == null) {
      newPosTag = postag;
    }
    return newPosTag;
  }
}
