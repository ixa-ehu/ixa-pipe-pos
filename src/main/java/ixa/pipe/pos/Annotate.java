/*
 * Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.pos;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;
import ixa.pipe.lemmatize.Dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ragerri
 *
 */
public class Annotate {

  private POS posTagger;


  public Annotate(String lang) throws IOException {
    Resources modelRetriever = new Resources();
    InputStream posModel = modelRetriever.getPOSModel(lang);
    posTagger = new POS(posModel);
  }


  /**
   *
   * Mapping between Penn Treebank tagset and KAF tagset
   *
   * @param penn treebank postag
   * @return kaf POS tag
   */
  private String mapEnglishTagSetToKaf(String postag) {
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

  private String mapSpanishTagSetToKaf(String postag) {
    if (postag.equalsIgnoreCase("RB") || postag.equalsIgnoreCase("RN")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC") || postag.equalsIgnoreCase("CS")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D")) {
      return "D"; // determiner and predeterminer
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

  public String getKafTagSet(String lang, String postag) {
    String tag = null;
    if (lang.equalsIgnoreCase("en")) {
      tag = this.mapEnglishTagSetToKaf(postag);
    }
    if (lang.equalsIgnoreCase("es")) {
      tag = this.mapSpanishTagSetToKaf(postag);
    }
    return tag;
  }


  /**
   * Set the term type attribute based on the pos value
   *
   * @param kaf postag
   * @return type
   */
  public String setTermType(String postag) {
    if (postag.startsWith("N") || postag.startsWith("V")
        || postag.startsWith("G") || postag.startsWith("A")) {
      return "open";
    } else {
      return "close";
    }
  }


  /**
   * This method uses the Apache OpenNLP to perform POS tagging.
   *
   * It gets a Map<SentenceId, tokens> from the input KAF document and iterates
   * over the tokens of each sentence to annotated POS tags.
   *
   * It also reads <wf>, elements from the input KAF document and fills the KAF
   * object with those elements plus the annotated POS tags in the <term>
   * elements.
   *
   * @param LinkedHashMap
   *          <String,List<String>
   * @param List
   *          <Element> termList
   * @param KAF
   *          object. This object is used to take the output data and convert it
   *          to KAF.
   *
   * @return JDOM KAF document containing <wf>, and <terms> elements.
   */



  public void annotatePOSToKAF(KAFDocument kaf, Dictionary dictLemmatizer, String lang)
              throws IOException {
    
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) { 
      
      /* Get an array of token forms from a list of WF objects. */
      String tokens[] = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
      }
      
      String [] posTagged = posTagger.posAnnotate(tokens);
      for (int i = 0; i < posTagged.length; i++) {
        List<WF> wfs = new ArrayList<WF>();
        wfs.add(sentence.get(i));
        String posTag = posTagged[i];
        String posId = this.getKafTagSet(lang, posTag);
        String type = this.setTermType(posId); // type
        String lemma = dictLemmatizer.lemmatize(lang, tokens[i], posTag); // lemma
        kaf.createTermOptions(type, lemma, posId, posTag,wfs);
      }
    }

  }

}
