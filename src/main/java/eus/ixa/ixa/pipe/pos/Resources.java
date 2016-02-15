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

package eus.ixa.ixa.pipe.pos;

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
   * Mapping between CoNLL 2009 German tagset and KAF tagset.
   * Based on the Stuttgart-Tuebingen tagset.
   * 
   * @param postag the postag
   * @return kaf POS tag
   */
  private static String mapGermanTagSetToKaf(final String postag) {
    if (postag.startsWith("ADV")) {
      return "A"; // adverb
    } else if (postag.startsWith("KO")) {
      return "C"; // conjunction
    } else if (postag.equalsIgnoreCase("ART")) {
      return "D"; // determiner and predeterminer
    } else if (postag.startsWith("ADJ")) {
      return "G"; // adjective
    } else if (postag.equalsIgnoreCase("NN")) {
      return "N"; // common noun
    } else if (postag.startsWith("NE")) {
      return "R"; // proper noun
    } else if (postag.startsWith("AP")) {
      return "P"; // preposition
    } else if (postag.startsWith("PD") || postag.startsWith("PI") || postag.startsWith("PP") || postag.startsWith("PR")
        || postag.startsWith("PW") || postag.startsWith("PA")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }
  /**
   * Mapping between Penn Treebank tagset and KAF tagset.
   * 
   * @param postag
   *          treebank postag
   * @return kaf POS tag
   */
  private static String mapEnglishTagSetToKaf(final String postag) {
    if (postag.startsWith("RB")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D") || postag.equalsIgnoreCase("PDT")) {
      return "D"; // determiner and predeterminer
    } else if (postag.startsWith("J")) {
      return "G"; // adjective
    } else if (postag.equalsIgnoreCase("NN") || postag.equalsIgnoreCase("NNS")) {
      return "N"; // common noun
    } else if (postag.startsWith("NNP")) {
      return "R"; // proper noun
    } else if (postag.equalsIgnoreCase("TO") || postag.equalsIgnoreCase("IN")) {
      return "P"; // preposition
    } else if (postag.startsWith("PRP") || postag.startsWith("WP")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }

  /**
   * Mapping between EAGLES PAROLE tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private static String mapSpanishTagSetToKaf(final String postag) {
    if (postag.equalsIgnoreCase("RG") || postag.equalsIgnoreCase("RN")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC") || postag.equalsIgnoreCase("CS")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D")) {
      return "D"; // det predeterminer
    } else if (postag.startsWith("A")) {
      return "G"; // adjective
    } else if (postag.startsWith("NC")) {
      return "N"; // common noun
    } else if (postag.startsWith("NP")) {
      return "R"; // proper noun
    } else if (postag.startsWith("SP")) {
      return "P"; // preposition
    } else if (postag.startsWith("P")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }
  
  /**
   * Mapping between Universal POS tags and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private static String mapUDTagSetToKaf(final String postag) {
    if (postag.equalsIgnoreCase("ADV")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CONJ") || postag.equalsIgnoreCase("SCONJ")) {
      return "C"; // conjunction
    } else if (postag.equalsIgnoreCase("DET")) {
      return "D"; // det predeterminer
    } else if (postag.equalsIgnoreCase("ADJ")) {
      return "G"; // adjective
    } else if (postag.equalsIgnoreCase("NOUN")) {
      return "N"; // common noun
    } else if (postag.equalsIgnoreCase("PROPN")) {
      return "R"; // proper noun
    } else if (postag.equalsIgnoreCase("ADP")) {
      return "P"; // preposition
    } else if (postag.equalsIgnoreCase("PRON")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }
  
  /**
   * Mapping between CC tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private static String mapFrenchTagSetToKaf(final String postag) {
    if (postag.startsWith("ADV")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC") || postag.equalsIgnoreCase("CS")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D") || postag.startsWith("I")) {
      return "D"; // det predeterminer
    } else if (postag.startsWith("ADJ")) {
      return "G"; // adjective
    } else if (postag.startsWith("NC")) {
      return "N"; // common noun
    } else if (postag.startsWith("NPP")) {
      return "R"; // proper noun
    } else if (postag.startsWith("PRO") || postag.startsWith("CL")) {
      return "Q"; // pronoun
    } else if (postag.equalsIgnoreCase("P") || postag.equalsIgnoreCase("P+D")
        || postag.equalsIgnoreCase("P+PRO")) {
      return "P"; // preposition
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }

  /**
   * Mapping between CTAG tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private static String mapGalicianTagSetToKaf(final String postag) {
    if (postag.startsWith("R")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC") || postag.equalsIgnoreCase("CS")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D") || postag.startsWith("G")
        || postag.startsWith("X") || postag.startsWith("Q")
        || postag.startsWith("T") || postag.startsWith("I")
        || postag.startsWith("M")) {
      return "D"; // det predeterminer
    } else if (postag.startsWith("A")) {
      return "G"; // adjective
    } else if (postag.startsWith("NC")) {
      return "N"; // common noun
    } else if (postag.startsWith("NP")) {
      return "R"; // proper noun
    } else if (postag.startsWith("S")) {
      return "P"; // preposition9434233199310741
    } else if (postag.startsWith("P")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }
  
  /**
   * Mapping between Wotan (Alpino) tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private static String mapWotanTagSetToKaf(final String postag) {
    if (postag.equalsIgnoreCase("ADV")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CONJ") || postag.equalsIgnoreCase("SCONJ")) {
      return "C"; // conjunction
    } else if (postag.equalsIgnoreCase("Art")) {
      return "D"; // det predeterminer
    } else if (postag.equalsIgnoreCase("ADJ")) {
      return "G"; // adjective
    } else if (postag.equalsIgnoreCase("N")) {
      return "N"; // common noun
    } else if (postag.equalsIgnoreCase("PROPN")) {
      return "R"; // proper noun
    } else if (postag.equalsIgnoreCase("Prep")) {
      return "P"; // preposition
    } else if (postag.equalsIgnoreCase("PRON")) {
      return "Q"; // pronoun
    } else if (postag.startsWith("V")) {
      return "V"; // verb
    } else {
      return "O"; // other
    }
  }
  
  

  /**
   * Obtain the appropriate tagset according to language and postag.
   * 
   * @param postag
   *          the postag
   * @param lang the language
   * @return the mapped tag
   */
  public static String getKafTagSet(final String postag, final String lang) {
    
    String tag = null;
    if (lang.equalsIgnoreCase("de")) {
      tag = mapGermanTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("en")) {
      tag = mapEnglishTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("es")) {
      tag = mapSpanishTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("eu")) {
      tag = mapUDTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("gl")) {
      tag = mapGalicianTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("fr")) {
      tag = mapFrenchTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("it")) {
      tag = mapUDTagSetToKaf(postag);
    } else if (lang.equalsIgnoreCase("nl")) {
      tag = mapWotanTagSetToKaf(postag);
    } else {
      tag = "O";
    }
    return tag;
  }

  /**
   * Set the term type attribute based on the pos value.
   * 
   * @param postag
   *          the postag
   * @return the type
   */
  public static String setTermType(final String postag) {
    if (postag.startsWith("N") || postag.startsWith("V")
        || postag.startsWith("G") || postag.startsWith("A")) {
      return "open";
    } else {
      return "close";
    }
  }

}
