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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.didion.jwnl.JWNLException;

import org.jdom2.Element;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private POS posTagger;
  private JWNLemmatizer wnLemmatizer;

  public Annotate(String lang, String wnDirectory) throws IOException,
      JWNLException {
    Models modelRetriever = new Models();
    InputStream posModel = modelRetriever.getPOSModel(lang);
    posTagger = new POS(posModel);
    wnLemmatizer = new JWNLemmatizer(wnDirectory);
  }

  /**
   * It reads the linguisticProcessor elements and adds them to the KAF
   * document.
   * 
   * @param lingProc
   * @param kaf
   */
  public void addKafHeader(List<Element> lingProc, KAF kaf) {
    String layer = null;
    for (int i = 0; i < lingProc.size(); i++) {
      layer = lingProc.get(i).getAttributeValue("layer");
      List<Element> lps = lingProc.get(i).getChildren("lp");
      for (Element lp : lps) {
        kaf.addlps(layer, lp.getAttributeValue("name"),
            lp.getAttributeValue("timestamp"), lp.getAttributeValue("version"));
      }
    }
  }

  /**
   * It obtains the term type attribute
   * 
   * @param postag
   * @return type
   */
  public String getTermType(String postag) {
    if (postag.startsWith("N") || postag.startsWith("V")
        || postag.startsWith("J") || postag.startsWith("RB")) {
      return "open";
    } else {
      return "close";
    }
  }

  /**
   * 
   * Mapping between Penn Treebank tagset and KAF tagset
   * 
   * @param penn treebank postag
   * @return kaf POS tag
   */
  public String getKafPosTag(String postag) {
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

  public void annotatePOSToKAF(
      LinkedHashMap<String, List<String>> sentTokensMap, KAF kaf)
      throws IOException {

    for (Map.Entry<String, List<String>> sentence : sentTokensMap.entrySet()) {
      String sid = sentence.getKey();
      String[] tokens = sentence.getValue().toArray(
          new String[sentence.getValue().size()]);

      // POS annotation
      String[] posTagged = posTagger.posAnnotate(tokens);

      // Add tokens in the sentence to kaf object
      int numTokensInKaf = kaf.getNumWfs();
      int nextTokenInd = numTokensInKaf + 1;
      for (int i = 0; i < tokens.length; i++) {
        String id = "w" + Integer.toString(nextTokenInd++);
        String tokenStr = tokens[i];
        kaf.addWf(id, sid, tokenStr);
      }

      // Add terms to KAF object
      int noTerms = kaf.getNumTerms();
      int realTermCounter = noTerms + 1;
      int noTarget = numTokensInKaf + 1;
      for (int j = 0; j < posTagged.length; j++) {
        String termId = "t" + Integer.toString(realTermCounter++); // termId
        String posTag = posTagged[j];
        String posId = this.getKafPosTag(posTag); // posId
        String type = this.getTermType(posTag); // type
        String lemma = wnLemmatizer.lemmatize(tokens[j], posTag); // lemma
        String spanString = tokens[j]; // spanString
        ArrayList<String> tokenIds = new ArrayList<String>();
        tokenIds.add("w" + Integer.toString(noTarget++)); // targets
        kaf.addTerm(termId, posId, type, lemma, tokenIds, spanString, posTag);
      }

    }
  }

}
