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
package eus.ixa.ixa.pipe.lemma;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eus.ixa.ixa.pipe.pos.Morpheme;
import eus.ixa.ixa.pipe.pos.MorphoFactory;

/**
 * Probabilistic lemmatizer.
 * 
 * @author ragerri
 * @version 2016-01-28
 */

public class StatisticalLemmatizer {

  /**
   * The lemmatizer.
   */
  private final LemmatizerME lemmatizer;
  /**
   * The models to use for every language. The keys of the hashmap are the language
   * codes, the values the models.
   */
  private static ConcurrentHashMap<String, LemmatizerModel> lemmaModels = new ConcurrentHashMap<String, LemmatizerModel>();
  /**
   * The morpho factory.
   */
  private MorphoFactory morphoFactory;

  /**
   * Construct a statistical lemmatizer.
   * 
   * @param props
   *          the properties object
   */
  public StatisticalLemmatizer(final Properties props) {
    final String lang = props.getProperty("language");
    final String model = props.getProperty("lemmatizerModel");
    final LemmatizerModel lemmatizerModel = loadModel(lang, model);
    this.lemmatizer = new LemmatizerME(lemmatizerModel);
  }

  /**
   * Construct a statistical lemmatizer with {@code MorphoFactory}.
   * 
   * @param props
   *          the properties object
   * @param aMorphoFactory
   *          the morpho factory
   */
  public StatisticalLemmatizer(final Properties props, final MorphoFactory aMorphoFactory) {
    final String lang = props.getProperty("language");
    final String model = props.getProperty("lemmatizerModel");
    final LemmatizerModel posModel = loadModel(lang, model);
    this.lemmatizer = new LemmatizerME(posModel);
    this.morphoFactory = aMorphoFactory;
  }

  /**
   * Get lemmas from a tokenized and pos tagged sentence.
   * 
   * @param tokens
   *          the tokenized sentence
   * @param posTags the pos tags of the sentence
   * @return a list of {@code Morpheme} objects containing morphological info
   */
  public final List<Morpheme> getMorphemes(final String[] tokens, final String[] posTags) {
    final List<String> lemmas = lemmatize(tokens, posTags);
    final List<Morpheme> morphemes = getMorphemesFromStrings(tokens, posTags, lemmas);
    return morphemes;
  }
  
  /**
   * Produce lemmas from a tokenized sentence and its postags.
   * @param tokens the tokens
   * @param posTags the pos tags
   * @return the lemmas
   */
  public List<String> lemmatize(String[] tokens, String[] posTags) {
    String[] annotatedLemmas = lemmatizer.lemmatize(tokens, posTags);
    String[] decodedLemmas = lemmatizer.decodeLemmas(tokens, annotatedLemmas);
    final List<String> lemmas = new ArrayList<String>(Arrays.asList(decodedLemmas));
    return lemmas;
  }

  /**
   * Create {@code Morpheme} objects from the output of posAnnotate.
   * @param tokens the tokens
   * @param posTags
   *          the postags
   * @param lemmas the lemmas
   * @return a list of morpheme objects
   */
  public final List<Morpheme> getMorphemesFromStrings(final String[] tokens, final String[] posTags, final List<String> lemmas) {
    final List<Morpheme> morphemes = new ArrayList<Morpheme>();
    for (int i = 0; i < posTags.length; i++) {
      final String word = tokens[i];
      final String tag = posTags[i];
      final String lemma = lemmas.get(i);
      final Morpheme morpheme = this.morphoFactory.createMorpheme(word, tag, lemma);
      morphemes.add(morpheme);
    }
    return morphemes;
  }
  
  /**
   * Takes a sentence with multiple tags alternatives for each word and produces
   * a lemma for each of the word-tag combinations.
   * @param tokens the sentence tokens
   * @param posTags the alternative postags
   * @return the ordered map containing all the possible tag#lemma values for token
   */
  public ListMultimap<String, String> getMultipleLemmas(String[] tokens, String[][] posTags) {
    
    ListMultimap<String, String> morphMap = ArrayListMultimap.create();
    for (int i = 0; i < posTags.length; i++) {
      String[] rowLemmas = this.lemmatizer.lemmatize(tokens, posTags[i]);
      String[] decodedLemmas = this.lemmatizer.decodeLemmas(tokens, rowLemmas);
      for (int j = 0; j < decodedLemmas.length; j++) {
        morphMap.put(tokens[j], posTags[i][j] + "#" + decodedLemmas[j]);
      }
    }
    return morphMap;
  }

  /**
   * Loads statically the probabilistic model. Every instance of this finder
   * will share the same model.
   * 
   * @param lang
   *          the language
   * @param model
   *          the model to be loaded
   * @return the model as a {@link LemmatizerModel} object
   */
  private LemmatizerModel loadModel(final String lang, final String model) {
    final long lStartTime = new Date().getTime();
    try {
      synchronized (lemmaModels) {
        if (!lemmaModels.containsKey(lang)) {
          lemmaModels.put(lang, new LemmatizerModel(new FileInputStream(model)));
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final long lEndTime = new Date().getTime();
    final long difference = lEndTime - lStartTime;
    System.err.println("ixa-pipe-lemma model loaded in: " + difference
        + " miliseconds ... [DONE]");
    return lemmaModels.get(lang);
  }

}
