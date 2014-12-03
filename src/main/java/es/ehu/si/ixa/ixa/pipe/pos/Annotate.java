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

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.util.Span;
import es.ehu.si.ixa.ixa.pipe.lemma.DictionaryLemmatizer;
import es.ehu.si.ixa.ixa.pipe.lemma.MorfologikLemmatizer;
import es.ehu.si.ixa.ixa.pipe.lemma.MultiWordMatcher;
import es.ehu.si.ixa.ixa.pipe.lemma.SimpleLemmatizer;

/**
 * Main annotation class of ixa-pipe-pos. Check this class for examples using
 * the ixa-pipe-pos API.
 * 
 * @author ragerri
 * @version 2014-07-09
 */
public class Annotate {

  /**
   * The morpho tagger.
   */
  private MorphoTagger posTagger;
  /**
   * The language.
   */
  private String lang;
  /**
   * The factory to build morpheme objects.
   */
  private MorphoFactory morphoFactory;
  /**
   * The dictionary lemmatizer.
   */
  private DictionaryLemmatizer dictLemmatizer;
  /**
   * If true detect multiwords.
   */
  private Boolean multiwords;
  /**
   * The multiword matcher.
   */
  private MultiWordMatcher multiWordMatcher;

  /**
   * Construct an annotator with a {@code MorphoFactory}.
   * 
   * @param properties
   *          the properties file
   * @throws IOException
   *           io exception if model not properly loaded
   */
  public Annotate(final Properties properties) throws IOException {
    this.lang = properties.getProperty("language");
    this.multiwords = Boolean.valueOf(properties.getProperty("multiwords"));
    if (multiwords) {
      multiWordMatcher = new MultiWordMatcher(properties);
    }
    loadResources(properties);
    morphoFactory = new MorphoFactory();
    posTagger = new MorphoTagger(properties, morphoFactory);
  }

  // TODO static loading of lemmatizer dictionaries
  private void loadResources(Properties props) {
    String lemmatize = props.getProperty("lemmatize");
    Resources resources = new Resources();
    if (lemmatize.equalsIgnoreCase("plain")) {
      InputStream simpleDictInputStream = resources.getDictionary(lang);
      dictLemmatizer = new SimpleLemmatizer(simpleDictInputStream, lang);
    }
    if (lemmatize.equalsIgnoreCase("bin")) {
      URL binLemmatizerURL = resources.getBinaryDict(lang);
      try {
        dictLemmatizer = new MorfologikLemmatizer(binLemmatizerURL, lang);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Mapping between Penn Treebank tagset and KAF tagset.
   * 
   * @param postag
   *          treebank postag
   * @return kaf POS tag
   */
  private String mapEnglishTagSetToKaf(final String postag) {
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
   * Mapping between Ancora EAGLES PAROLE tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private String mapSpanishTagSetToKaf(final String postag) {
    if (postag.equalsIgnoreCase("RB") || postag.equalsIgnoreCase("RN")) {
      return "A"; // adverb
    } else if (postag.equalsIgnoreCase("CC") || postag.equalsIgnoreCase("CS")) {
      return "C"; // conjunction
    } else if (postag.startsWith("D")) {
      return "D"; // det * @param dictLemmatizer the lemmatizererminer and
                  // predeterminer
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
   * Obtain the appropriate tagset according to language and postag.
   * 
   * @param postag
   *          the postag
   * @return the mapped tag
   */
  private String getKafTagSet(final String postag) {
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
   * Set the term type attribute based on the pos value.
   * 
   * @param postag
   *          the postag
   * @return the type
   */
  private String setTermType(final String postag) {
    if (postag.startsWith("N") || postag.startsWith("V")
        || postag.startsWith("G") || postag.startsWith("A")) {
      return "open";
    } else {
      return "close";
    }
  }

  /**
   * Annotate morphological information into a NAF document.
   * @param kaf the NAF document
   */
  public final void annotatePOSToKAF(final KAFDocument kaf) {
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> wfs : sentences) {
      
      List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
      List<Morpheme> morphemes = null;
      String[] tokens = new String[wfs.size()];
      for (int i = 0; i < wfs.size(); i++) {
        tokens[i] = wfs.get(i).getForm();
        List<WF> wfTarget = new ArrayList<WF>();
        wfTarget.add(wfs.get(i));
        tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
      }
      if (multiwords) {
        String[] multiWordTokens = multiWordMatcher.getTokensWithMultiWords(tokens);
        morphemes = posTagger.getMorphemes(multiWordTokens);
        getMultiWordSpans(tokens, wfs, tokenSpans);
      } else {
        morphemes = posTagger.getMorphemes(tokens);
      }
      for (int i = 0; i < morphemes.size(); i++) {
        Term term = kaf.newTerm(tokenSpans.get(i));
        String posId = this.getKafTagSet(morphemes.get(i).getTag());
        String type = this.setTermType(posId);
        String lemma = dictLemmatizer.lemmatize(morphemes.get(i).getWord(),
            morphemes.get(i).getTag());
        morphemes.get(i).setLemma(lemma);
        term.setType(type);
        term.setLemma(morphemes.get(i).getLemma());
        term.setPos(posId);
        term.setMorphofeat(morphemes.get(i).getTag());
        System.err.println(morphemes.get(i).getWord() + ": " + tokenSpans.get(i).toString());
      }
    }
  }
  
  private void getMultiWordSpans(String[] tokens, List<WF> wfs, List<ixa.kaflib.Span<WF>> tokenSpans) {
    Span[] multiWordSpans = multiWordMatcher.multiWordsToSpans(tokens);
    int counter = 0;
    for (Span mwSpan : multiWordSpans) {
      Integer startIndex = mwSpan.getStart();
      Integer endIndex = mwSpan.getEnd();
      //create multiword targets
      List<WF> wfTargets = wfs.subList(startIndex, endIndex);
      ixa.kaflib.Span<WF> multiWordSpan = KAFDocument.newWFSpan(wfTargets);
      //remove the WF spans corresponding to the multiword span
      tokenSpans.subList(startIndex, endIndex).clear();
      //add the new Span containing several WFs (multiWordSpan)
      tokenSpans.add(startIndex - counter, multiWordSpan);
      counter =+ mwSpan.length() - 1;
    }
  }

  /**
   * Annotate morphological information in tabulated CoNLL-style format.
   * 
   * @param kaf
   *          the naf input document
   * @return the text annotated in tabulated format
   * @throws IOException
   *           throws io exception
   */
  public final String annotatePOSToCoNLL(final KAFDocument kaf)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      // Get an array of token forms from a list of WF objects.
      String[] tokens = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
      }
      List<String> posTagged = posTagger.posAnnotate(tokens);
      for (int i = 0; i < posTagged.size(); i++) {
        String posTag = posTagged.get(i);
        String lemma = dictLemmatizer.lemmatize(tokens[i], posTag); // lemma
        sb.append(tokens[i]).append("\t").append(lemma).append("\t")
            .append(posTag).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  
  /*public final void annotatePOSToKAF(final KAFDocument kaf) throws IOException {

  List<List<WF>> sentences = kaf.getSentences();
  for (List<WF> wfs : sentences) {
    
    List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
    String[] tokens = new String[wfs.size()];
    for (int i = 0; i < wfs.size(); i++) {
      tokens[i] = wfs.get(i).getForm();
      List<WF> wfTarget = new ArrayList<WF>();
      wfTarget.add(wfs.get(i));
      tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
    }
    List<Morpheme> morphemes = posTagger.getMorphemes(tokens);
    for (int i = 0; i < morphemes.size(); i++) {
      ixa.kaflib.Span<WF> wfSpan = tokenSpans.get(i);
      Term tokenTerm = kaf.newTerm(wfSpan);
      String posId = this.getKafTagSet(morphemes.get(i).getTag());
      String type = this.setTermType(posId);
      String lemma = dictLemmatizer.lemmatize(morphemes.get(i).getWord(),
          morphemes.get(i).getTag());
      morphemes.get(i).setLemma(lemma);
      tokenTerm.setType(type);
      tokenTerm.setLemma(morphemes.get(i).getLemma());
      tokenTerm.setPos(posId);
      tokenTerm.setMorphofeat(morphemes.get(i).getTag());

    }
  }
}*/


}
