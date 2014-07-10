/*Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.pipe.pos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 * POS tagging module based on Apache OpenNLP machine learning API.
 * @author ragerri
 * @version 2014-04-24
 */

public class MorphoTagger {

  /**
   * The morpho tagger.
   */
  private POSTaggerME posTagger;
  /**
   * The models to use for every language. The keys of the hash are the
   * language codes, the values the models.
   */
  private static ConcurrentHashMap<String, POSModel> posModels =
      new ConcurrentHashMap<String, POSModel>();
  /**
   * The morpho factory.
   */
  private MorphoFactory morphoFactory;
  /**
   * The language.
   */
  private String lang;

  /**
   * Construct a morphotagger.
   * @param aLang the language
   * @param model the model
   * @param beamsize the beamsize
   */
  public MorphoTagger(final String aLang, final String model, final int beamsize) {
    this.lang = aLang;
    POSModel posModel = loadModel(model);
    posTagger = new POSTaggerME(posModel, beamsize, 0);
  }

  /**
   * Construct a morphotagger with default beamsize.
   * @param aLang the language
   * @param model the model
   */
  public MorphoTagger(final String aLang, final String model) {
    this(aLang, model, CLI.DEFAULT_BEAM_SIZE);
  }

  /**
   * Construct a morphotagger with {@code MorphoFactory}.
   * @param aLang the language
   * @param model the model
   * @param beamsize the beamsize
   * @param aMorphoFactory the morpho factory
   */
  public MorphoTagger(final String aLang, final String model, final int beamsize, final MorphoFactory aMorphoFactory) {
    this.lang = aLang;
    POSModel posModel = loadModel(model);
    posTagger = new POSTaggerME(posModel, beamsize, 0);
    this.morphoFactory = aMorphoFactory;
  }

  /**
   * Construct a morphotagger with default beamsize and morpho factory.
   * @param aLang the language
   * @param model the model
   * @param aMorphoFactory the factory
   */
  public MorphoTagger(final String aLang, final String model, final MorphoFactory aMorphoFactory) {
    this(aLang, model, CLI.DEFAULT_BEAM_SIZE, aMorphoFactory);
  }

  /**
   * Get morphological analysis from a tokenized sentence.
   * @param tokens the tokenized sentence
   * @return a list of {@code Morpheme} objects containing morphological info
   */
  public final List<Morpheme> getMorphemes(final String[] tokens) {
    List<String> origPosTags = posAnnotate(tokens);
    List<Morpheme> morphemes = getMorphemesFromStrings(origPosTags, tokens);
    return morphemes;
  }
  /**
   * Produce postags from a tokenized sentence.
   * @param tokens the sentence
   * @return a list containing the postags
   */
  public final List<String> posAnnotate(final String[] tokens) {
    String[] annotatedText = posTagger.tag(tokens);
    List<String> posTags = new ArrayList<String>(Arrays.asList(annotatedText));
    return posTags;
  }

  /**
   * Create {@code Morpheme} objects from the output of posAnnotate.
   * @param posTags the postags
   * @param tokens the tokens
   * @return a list of morpheme objects
   */
  public final List<Morpheme> getMorphemesFromStrings(final List<String> posTags, final String[] tokens) {
    List<Morpheme> morphemes = new ArrayList<Morpheme>();
    for (int i = 0; i < posTags.size(); i++) {
      String word = tokens[i];
      String tag = posTags.get(i);
      Morpheme morpheme = morphoFactory.createMorpheme(word, tag);
      morphemes.add(morpheme);
    }
    return morphemes;
  }

  /**
   * Load model statically only if a model for the specified language is not already there.
   * @param model the model type
   * @return the model
   */
  private POSModel loadModel(final String model) {
    InputStream trainedModelInputStream = null;
    try {
      if (!posModels.containsKey(lang)) {
        if (model.equalsIgnoreCase("baseline")) {
          trainedModelInputStream = getBaselineModelStream(model);
        } else {
          trainedModelInputStream = new FileInputStream(model);
        }
        posModels.put(lang, new POSModel(trainedModelInputStream));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
    return posModels.get(lang);
  }

  /**
   * Back-off to baseline models for a language.
   * @param model the model type
   * @return the back-off model
   */
  private InputStream getBaselineModelStream(final String model) {
    InputStream trainedModelInputStream = null;
    if (lang.equalsIgnoreCase("en")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/en/en-pos-perceptron-c0-b3-dev.bin");
    }
    if (lang.equalsIgnoreCase("es")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/es/es-pos-perceptron-c0-b3.bin");
    }
    return trainedModelInputStream;
  }

}
