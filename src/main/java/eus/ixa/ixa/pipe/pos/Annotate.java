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

import com.google.common.collect.ListMultimap;

import eus.ixa.ixa.pipe.lemma.StatisticalLemmatizer;
import eus.ixa.ixa.pipe.lemma.dict.MorfologikLemmatizer;
import eus.ixa.ixa.pipe.pos.dict.DictionaryTagger;
import eus.ixa.ixa.pipe.pos.dict.MorfologikTagger;
import eus.ixa.ixa.pipe.pos.dict.MultiWordMatcher;

/**
 * Example annotation class of ixa-pipe-pos. Check this class for examples using
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
  /**
   * The statistical lemmatizer.
   */
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
  private MorfologikLemmatizer dictLemmatizer;
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
          .println("WARNING: No lemmatizer dictionary available for language "
              + this.lang + " in src/main/resources!");
    } else {
      try {
        this.dictLemmatizer = new MorfologikLemmatizer(binLemmatizerURL);
      } catch (final IOException e) {
        e.printStackTrace();
      }
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
      this.dictMorphoTagger = new MorfologikTagger(binDictMorphoTaggerURL,
          this.lang);
    } catch (final IOException e) {
      e.printStackTrace();
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
        final String posId = Resources.getKafTagSet(morphemes.get(i).getTag(), lang);
        final String type = Resources.setTermType(posId);
        // dictionary lemmatizer overwrites probabilistic predictions if
        // lemma is not equal to "O"
        if (this.dictLemmatizer != null) {
          final String lemma = this.dictLemmatizer.apply(morphemes.get(i)
              .getWord(), morphemes.get(i).getTag());
          if (!lemma.equalsIgnoreCase("O")) {
            morphemes.get(i).setLemma(lemma);
          }
        }
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
        // dictionary lemmatizer overwrites probabilistic predictions
        // if lemma is not equal to word
        if (this.dictLemmatizer != null) {
          final String lemma = this.dictLemmatizer.apply(word, morphemes.get(i)
              .getTag());
          if (!lemma.equalsIgnoreCase("O")) {
            morphemes.get(i).setLemma(lemma);
          }
        }
        sb.append(word).append("\t").append(morphemes.get(i).getLemma())
            .append("\t").append(morphemes.get(i).getTag()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  
  /**
   * Add all postags and lemmas to morphofeat attribute.
   * @param kaf the NAF document
   */
  public final void getAllTagsLemmasToNAF(final KAFDocument kaf) {
    final List<List<WF>> sentences = kaf.getSentences();
    for (final List<WF> wfs : sentences) {

      final List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
      final String[] tokens = new String[wfs.size()];
      for (int i = 0; i < wfs.size(); i++) {
        tokens[i] = wfs.get(i).getForm();
        final List<WF> wfTarget = new ArrayList<WF>();
        wfTarget.add(wfs.get(i));
        tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
      }
      
      String[][] allPosTags = this.posTagger.getAllPosTags(tokens);
      ListMultimap<String, String> morphMap = lemmatizer.getMultipleLemmas(tokens, allPosTags);
      
      for (int i = 0; i < tokens.length; i++) {
        final Term term = kaf.newTerm(tokenSpans.get(i));
        List<String> posLemmaValues = morphMap.get(tokens[i]);
        if (this.dictLemmatizer != null) {
          dictLemmatizer.getAllPosLemmas(tokens[i], posLemmaValues);
        }
        String allPosLemmasSet = StringUtils.getSetStringFromList(posLemmaValues);
        final String posId = Resources.getKafTagSet(allPosTags[0][i], lang);
        final String type = Resources.setTermType(posId);
        term.setType(type);
        term.setLemma(posLemmaValues.get(0).split("#")[1]);
        term.setPos(posId);
        term.setMorphofeat(allPosLemmasSet);
      }
    }
  }
  
  /**
   * Give all lemmas and tags possible for a sentence in conll tabulated format.
   * @param kaf the NAF document
   * @return the output in tabulated format
   */
  public final String getAllTagsLemmasToCoNLL(final KAFDocument kaf) {
    final StringBuilder sb = new StringBuilder();
    final List<List<WF>> sentences = kaf.getSentences();
    for (final List<WF> wfs : sentences) {

      final List<ixa.kaflib.Span<WF>> tokenSpans = new ArrayList<ixa.kaflib.Span<WF>>();
      final String[] tokens = new String[wfs.size()];
      for (int i = 0; i < wfs.size(); i++) {
        tokens[i] = wfs.get(i).getForm();
        final List<WF> wfTarget = new ArrayList<WF>();
        wfTarget.add(wfs.get(i));
        tokenSpans.add(KAFDocument.newWFSpan(wfTarget));
      }
      
      String[][] allPosTags = this.posTagger.getAllPosTags(tokens);
      ListMultimap<String, String> morphMap = lemmatizer.getMultipleLemmas(tokens, allPosTags);
      for (int i = 0; i < tokens.length; i++) {
        List<String> posLemmaValues = morphMap.get(tokens[i]);
        if (this.dictLemmatizer != null) {
          dictLemmatizer.getAllPosLemmas(tokens[i], posLemmaValues);
        }
        String allPosLemmasSet = StringUtils.getSetStringFromList(posLemmaValues);
        sb.append(tokens[i]).append("\t").append(allPosLemmasSet).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
