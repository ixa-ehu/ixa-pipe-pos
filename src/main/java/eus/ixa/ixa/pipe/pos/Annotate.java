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

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.util.Span;
import eus.ixa.ixa.pipe.lemma.StatisticalLemmatizer;
import eus.ixa.ixa.pipe.lemma.dict.DictionaryLemmatizer;
import eus.ixa.ixa.pipe.lemma.dict.MorfologikLemmatizer;
import eus.ixa.ixa.pipe.pos.dict.DictionaryTagger;
import eus.ixa.ixa.pipe.pos.dict.MorfologikTagger;
import eus.ixa.ixa.pipe.pos.dict.MultiWordMatcher;

/**
 * Main annotation class of ixa-pipe-pos. Check this class for examples using
 * the ixa-pipe-pos API.
 * 
 * @author ragerri
 * @version 2014-12-05
 */
public class Annotate {

  /**
   * The morpho tagger.
   */
  private final StatisticalTagger posTagger;
  private final StatisticalLemmatizer lemmatizer;
  /**
   * The language.
   */
  private final String lang;
  /**
   * The factory to build morpheme objects.
   */
  private final MorphoFactory morphoFactory;
  /**
   * The dictionary lemmatizer.
   */
  private DictionaryLemmatizer dictLemmatizer;
  /**
   * If true detect multiwords.
   */
  private final Boolean multiwords;
  /**
   * The multiword matcher.
   */
  private MultiWordMatcher multiWordMatcher;
  /**
   * If true detect postprocess tagger output with monosemic dictionary.
   */
  private final Boolean dictag;
  /**
   * The monosemic dictionary postagger.
   */
  private DictionaryTagger dictMorphoTagger;

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
    this.dictag = Boolean.valueOf(properties.getProperty("dictag"));
    if (this.multiwords) {
      this.multiWordMatcher = new MultiWordMatcher(properties);
    }
    if (this.dictag) {
      loadMorphoTaggerDicts(properties);
    }
    loadLemmatizerDicts(properties);
    this.morphoFactory = new MorphoFactory();
    this.posTagger = new StatisticalTagger(properties, this.morphoFactory);
    this.lemmatizer = new StatisticalLemmatizer(properties, this.morphoFactory);
  }

  // TODO static loading of lemmatizer dictionaries
  /**
   * Load the binary lemmatizer dictionaries by language. Exits if no lemmatizer
   * dictionary (binary) is available for the input language.
   * 
   * @param props
   *          the props object
   */
  private void loadLemmatizerDicts(final Properties props) {
    final Resources resources = new Resources();
    final URL binLemmatizerURL = resources.getBinaryDict(this.lang);
    if (binLemmatizerURL == null) {
      System.err
          .println("ERROR: No binary lemmatizer dictionary available for language "
              + this.lang + " in src/main/resources!!");
      System.exit(1);
    }
    try {
      this.dictLemmatizer = new MorfologikLemmatizer(binLemmatizerURL,
          this.lang);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  // TODO static loading of postag dictionaries
  /**
   * Load the pos tagger dictionaries by language and format. Exits if no pos
   * tagger dictionary (binary) is available for the input language.
   * 
   * @param props
   *          the props object
   */
  private void loadMorphoTaggerDicts(final Properties props) {
    final Resources resources = new Resources();
    final URL binDictMorphoTaggerURL = resources.getBinaryTaggerDict(this.lang);
    if (binDictMorphoTaggerURL == null) {
      System.err
          .println("ERROR: No binary POS tagger dictionary available for language "
              + this.lang + " in src/main/resources!!");
      System.exit(1);
    }
    try {
      this.dictMorphoTagger = new MorfologikTagger(
          binDictMorphoTaggerURL, this.lang);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Mapping between Penn Treebank tagset and KAF ta9434233199310741gset.
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
   * Mapping between EAGLES PAROLE tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private String mapSpanishTagSetToKaf(final String postag) {
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
      return "Q"; // pronoun9434233199310741
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
  private String mapGalicianTagSetToKaf(final String postag) {
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
   * Mapping between CC tagset and NAF.
   * 
   * @param postag
   *          the postag
   * @return the mapping to NAF pos tagset
   */
  private String mapFrenchTagSetToKaf(final String postag) {
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
    } else if (postag.equalsIgnoreCase("P") || postag.equalsIgnoreCase("P+D") || postag.equalsIgnoreCase("P+PRO")) {
      return "P"; // preposition
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
    if (this.lang.equalsIgnoreCase("en")) {
      tag = this.mapEnglishTagSetToKaf(postag);
    }
    if (this.lang.equalsIgnoreCase("es")) {
      tag = this.mapSpanishTagSetToKaf(postag);
    }
    if (this.lang.equalsIgnoreCase("eu")) {
      tag = this.mapEnglishTagSetToKaf(postag);
    }
    if (this.lang.equalsIgnoreCase("gl")) {
      tag = this.mapGalicianTagSetToKaf(postag);
    }
    if  (this.lang.equalsIgnoreCase("fr")) {
      tag = this.mapFrenchTagSetToKaf(postag);
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
   * 
   * @param kaf
   *          the NAF document
   */
  public final void annotatePOSToKAF(final KAFDocument kaf) {
    final List<List<WF>> sentences = kaf.getSentences();
    for (final List<WF> wfs : sentences) {

      final List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
      List<Morpheme> morphemes = null;
      final String[] tokens = new String[wfs.size()];
      for (int i = 0; i < wfs.size(); i++) {
        tokens[i] = wfs.get(i).getForm();
        final List<WF> wfTarget = new ArrayList<WF>();
        wfTarget.add(wfs.get(i));
        tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
      }
      if (this.multiwords) {
        final String[] multiWordTokens = this.multiWordMatcher
            .getTokensWithMultiWords(tokens);
        morphemes = this.posTagger.getMorphemes(multiWordTokens);
        getMultiWordSpans(tokens, wfs, tokenSpans);
      } else {
        List<String> posTags = this.posTagger.posAnnotate(tokens);
        String[] posTagsArray = new String[posTags.size()];
        posTagsArray = posTags.toArray(posTagsArray);
        morphemes = this.lemmatizer.getMorphemes(tokens, posTagsArray);
      }
      for (int i = 0; i < morphemes.size(); i++) {
        final Term term = kaf.newTerm(tokenSpans.get(i));
        if (this.dictag) {
          final String dictPosTag = this.dictMorphoTagger.tag(morphemes.get(i)
              .getWord(), morphemes.get(i).getTag());
          morphemes.get(i).setTag(dictPosTag);
        }
        final String posId = this.getKafTagSet(morphemes.get(i).getTag());
        final String type = this.setTermType(posId);
        final String lemma = this.dictLemmatizer.lemmatize(morphemes.get(i)
            .getWord(), morphemes.get(i).getTag());
        morphemes.get(i).setLemma(lemma);
        term.setType(type);
        term.setLemma(morphemes.get(i).getLemma());
        term.setPos(posId);
        term.setMorphofeat(morphemes.get(i).getTag());
      }
    }
  }

  /**
   * Creates the multiword spans. It gets an initial list of spans (one per
   * token) and creates a multiword span when a multiword is detected.
   * 
   * @param tokens
   *          the list of tokens
   * @param wfs
   *          the list of WFs
   * @param tokenSpans
   *          the list of initial token spans
   */
  private void getMultiWordSpans(final String[] tokens, final List<WF> wfs,
      final List<ixa.kaflib.Span<WF>> tokenSpans) {
    final Span[] multiWordSpans = this.multiWordMatcher
        .multiWordsToSpans(tokens);
    int counter = 0;
    for (final Span mwSpan : multiWordSpans) {
      final Integer fromIndex = mwSpan.getStart() - counter;
      final Integer toIndex = mwSpan.getEnd() - counter;
      // add to the counter the length of the span removed
      counter = counter + tokenSpans.subList(fromIndex, toIndex).size() - 1;
      // create multiword targets and Span
      final List<WF> wfTargets = wfs
          .subList(mwSpan.getStart(), mwSpan.getEnd());
      final ixa.kaflib.Span<WF> multiWordSpan = KAFDocument
          .newWFSpan(wfTargets);
      // remove the token Spans to be replaced by the multiword span
      tokenSpans.subList(fromIndex, toIndex).clear();
      // add the new Span containing several WFs (multiWordSpan)
      // the counter is used to allow matching the spans to the
      // tokenSpans list indexes
      tokenSpans.add(fromIndex, multiWordSpan);
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
    final StringBuilder sb = new StringBuilder();
    final List<List<WF>> sentences = kaf.getSentences();
    for (final List<WF> wfs : sentences) {

      final List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
      List<Morpheme> morphemes = null;
      // Get an array of token forms from a list of WF objects.
      final String[] tokens = new String[wfs.size()];
      for (int i = 0; i < wfs.size(); i++) {
        tokens[i] = wfs.get(i).getForm();
        final List<WF> wfTarget = new ArrayList<WF>();
        wfTarget.add(wfs.get(i));
        tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
      }
      if (this.multiwords) {
        final String[] multiWordTokens = this.multiWordMatcher
            .getTokensWithMultiWords(tokens);
        morphemes = this.posTagger.getMorphemes(multiWordTokens);
        getMultiWordSpans(tokens, wfs, tokenSpans);
      } else {
        List<String> posTags = this.posTagger.posAnnotate(tokens);
        String[] posTagsArray = new String[posTags.size()];
        posTagsArray = posTags.toArray(posTagsArray);
        morphemes = this.lemmatizer.getMorphemes(tokens, posTagsArray);
      }
      for (int i = 0; i < morphemes.size(); i++) {
        final String posTag = morphemes.get(i).getTag();
        final String word = morphemes.get(i).getWord();
        if (this.dictag) {
          final String dictPosTag = this.dictMorphoTagger.tag(word, posTag);
          morphemes.get(i).setTag(dictPosTag);
        }
        //final String lemma = this.dictLemmatizer.lemmatize(word,
        //    morphemes.get(i).getTag());
        sb.append(word).append("\t").append(morphemes.get(i).getLemma()).append("\t")
            .append(morphemes.get(i).getTag()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
