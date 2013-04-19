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

import ixa.pipe.kaf.KAF;
import ixa.pipe.kaf.KAFUtils;
import ixa.pipe.lemmatize.Dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private POS posTagger;
  private KAFUtils kafUtils;


  public Annotate(String lang) throws IOException {
    Resources modelRetriever = new Resources();
    InputStream posModel = modelRetriever.getPOSModel(lang);
    posTagger = new POS(posModel);
    kafUtils = new KAFUtils();
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
    return "O";
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
	     List<Element> wfs, KAF kaf, Dictionary dictLemmatizer, String lang)
	      throws IOException, JDOMException {

    LinkedHashMap<String, List<String>> sentencesMap = kafUtils
        .getSentencesMap(wfs);
    LinkedHashMap<String, List<String>> sentTokensMap = kafUtils
        .getSentsFromWfs(sentencesMap, wfs);  
	    for (Map.Entry<String, List<String>> sentence : sentTokensMap.entrySet()) {
	      String sid = sentence.getKey();
	      String[] tokens = sentence.getValue().toArray(
	          new String[sentence.getValue().size()]);

	      // POS annotation
	      String[] posTagged = posTagger.posAnnotate(tokens);

	      // Add tokens in the sentence to kaf object
	      int numTokensInKaf = kaf.getNumWfs();
	      int indexNumTokens = numTokensInKaf + 1;
	      for (int i = 0; i < tokens.length; i++) {
	        int origWfCounter = i + numTokensInKaf;
	        int realWfCounter = i + indexNumTokens;
	        String offset = kafUtils.getWfOffset(wfs, origWfCounter);
	        String tokLength = kafUtils.getWfLength(wfs, origWfCounter);
	        String para = kafUtils.getWfPara(wfs, origWfCounter);
	        String id = "w" + Integer.toString(realWfCounter);
	        String tokenStr = tokens[i];
	        kaf.addWf(id, sid, offset, tokLength, para, tokenStr);
	      }

	      // Add terms to KAF object
	      int noTerms = kaf.getNumTerms();
	      int indexTermCounter = noTerms + 1;
	      for (int j = 0; j < posTagged.length; j++) {
	        int realWfCounter = j + indexNumTokens;
	        int realTermCounter = j + indexTermCounter;
	        String termId = "t" + Integer.toString(realTermCounter); // termId
	        String posTag = posTagged[j];
	        String posId = this.getKafTagSet(lang, posTag); // posId
	        String type = kafUtils.setTermType(posId); // type
	        String lemma = dictLemmatizer.lemmatize(lang, tokens[j], posTag); // lemma
	        String spanString = tokens[j]; // spanString
	        ArrayList<String> tokenIds = new ArrayList<String>();
	        tokenIds.add("w" + Integer.toString(realWfCounter)); // targets
	        kaf.addTerm(termId, posId, type, lemma, tokenIds, spanString, posTag);
	      }

	    }
	  }
  
}
