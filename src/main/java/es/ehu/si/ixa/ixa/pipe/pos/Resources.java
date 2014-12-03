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

package es.ehu.si.ixa.ixa.pipe.pos;

import java.io.InputStream;
import java.net.URL;

/**
 * Class to load the appropriate lemmatization dictionaries according to the
 * input language.
 *
 * @author ragerri
 * @version 2014-07-08
 *
 */
public class Resources {

  /**
   * Get the dictionary for the {@code SimpleLemmatizer}.
   *
   * @param lang
   *          the language
   * @return the inputstream of the dictionary
   */
  public final InputStream getDictionary(final String lang) {
    InputStream dict = null;
    if (lang.equalsIgnoreCase("en")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/language-tool/en-lemmatizer.dict");
    }
    if (lang.equalsIgnoreCase("es")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/freeling/es-lemmatizer.dict");
    } else {
      System.err.println("There is not dictionary for that language in the src/main/resources directory!!");
      System.exit(1);
    }
    return dict;
  }

  /**
   * The the dictionary for the {@code MorfologikLemmatizer}.
   * @param lang the language
   * @return the URL of the dictonary
   */
  public final URL getBinaryDict(final String lang) {
    URL dictURL = null;
    if (lang.equalsIgnoreCase("en")) {
      dictURL = getClass().getResource(
          "/lemmatizer-dicts/language-tool/english.dict");
    }
    if (lang.equalsIgnoreCase("es")) {
      dictURL = getClass().getResource(
          "/lemmatizer-dicts/freeling/spanish.dict");
    }
    if (lang.equalsIgnoreCase("gl")) {
      dictURL = getClass().getResource(
          "/lemmatizer-dicts/freeling/spanish.dict");
    } else {
      System.err.println("There is not dictionary for that language in the src/main/resources directory!!");
      System.exit(1);
    }
    return dictURL;
  }

  /**
   * Ad-hoc assignment of constant tags, typically proper names.
   * @param lang the language
   * @param postag the tag
   * @return the constant tag
   */
  public final String setTagConstant(final String lang, final String postag) {
    String constantTag = null;
    if (lang.equalsIgnoreCase("en")) {
      if (postag.equalsIgnoreCase("NNP")) {
        constantTag = "NNP";
      }
    }
    if (lang.equalsIgnoreCase("es")) {
      if (postag.startsWith("NP")) {
        constantTag = "NP00000";
      }
    }
    if (lang.equalsIgnoreCase("gl")) {
      if (postag.startsWith("NP")) {
        constantTag = "NP00000";
      }
    }
    return constantTag;
  }
}
