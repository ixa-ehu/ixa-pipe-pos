/*
 * Copyright 2016 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.pos;

import java.io.InputStream;
import java.net.URL;

/**
 * Class to load the appropriate dictionaries according to the
 * input language.
 * @author ragerri
 * @version 2016-04-20
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
          "/lemmatizer-dicts/language-tool/en-lemmatizer.txt");
    }
    if (lang.equalsIgnoreCase("es")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/freeling/es-lemmatizer.txt");
    }
    if (lang.equalsIgnoreCase("gl")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/ctag/gl-lemmatizer.txt");
    }
    return dict;
  }

  /**
   * The the dictionary for the {@code MorfologikLemmatizer}.
   * 
   * @param lang
   *          the language
   * @return the URL of the dictonary
   */
  public final URL getBinaryDict(final String lang) {
    URL dictURL = null;
    
    if (lang.equalsIgnoreCase("de")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/de/german.dict");
    } else if (lang.equalsIgnoreCase("en")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/language-tool/english.dict");
    } else if (lang.equalsIgnoreCase("es")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/freeling/spanish.dict");
    } else if (lang.equalsIgnoreCase("eu")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/eu/basque.dict");
    } else if (lang.equalsIgnoreCase("fr")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/fr/french.dict");
    } else if (lang.equalsIgnoreCase("gl")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/ctag/galician.dict");
    } else if (lang.equalsIgnoreCase("it")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/it/italian.dict");
    } else if (lang.equalsIgnoreCase("nl")) {
      dictURL = getClass().getResource("/lemmatizer-dicts/nl/dutch.dict");
    }
    return dictURL;
  }

  /**
   * The the dictionary for the {@code MorfologikMorphoTagger}.
   * 
   * @param lang
   *          the language
   * @return the URL of the dictionary
   */
  public final URL getBinaryTaggerDict(final String lang) {
    URL dictURL = null;
    if (lang.equalsIgnoreCase("es")) {
      dictURL = getClass().getResource(
          "/lemmatizer-dicts/freeling/spanish-monosemic.dict");
    }
    if (lang.equalsIgnoreCase("gl")) {
      dictURL = getClass().getResource(
          "/lemmatizer-dicts/ctag/galician-monosemic.dict");
    }
    return dictURL;
  }
  
  /**
   * Get the dictionary for the {@code MultiWordMatcher}.
   * 
   * @param lang
   *          the language
   * @return the inputstream of the dictionary
   */
  public final InputStream getMultiWordDict(final String lang) {
    InputStream dict = null;
    // TODO complete locutions dictionary and binarize
    if (lang.equalsIgnoreCase("en")) {
      dict = getClass().getResourceAsStream(
          "/lemmatizer-dicts/freeling/en-locutions-extended.txt");
    }
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
}
