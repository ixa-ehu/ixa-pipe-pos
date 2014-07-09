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
 * 
 * @author ragerri
 * @version 2014-04-24
 */

public class MorphoTagger {

  private POSTaggerME posTagger;
  /**
   * The models to use for every language. The keys of the hash are the
   * language codes, the values the models.
   */
  private static ConcurrentHashMap<String, POSModel> posModels =
      new ConcurrentHashMap<String, POSModel>();
  
  private MorphoFactory morphoFactory;

  /**
   * It constructs an object POS from the POS class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public MorphoTagger(final String lang, final String model, final int beamsize) {

    POSModel posModel = loadModel(lang, model);
    posTagger = new POSTaggerME(posModel, beamsize, 0);
  }
  
  public MorphoTagger(final String lang, final String model, final String features) {
    this(lang, model, CLI.DEFAULT_BEAM_SIZE);
  }
  
  public MorphoTagger(final String lang, final String model, final int beamsize, final MorphoFactory aMorphoFactory) {

    POSModel posModel = loadModel(lang, model);
    posTagger = new POSTaggerME(posModel, beamsize, 0);
    this.morphoFactory = aMorphoFactory;
  }
  
  public MorphoTagger(final String lang, final String model, final MorphoFactory aMorphoFactory) {
    this(lang, model, CLI.DEFAULT_BEAM_SIZE, aMorphoFactory);
  }

  public List<Morpheme> getMorphemes(String[] tokens) {
    List<String> origPosTags = posAnnotate(tokens);
    List<Morpheme> morphemes = getMorphemesFromStrings(origPosTags, tokens);
    return morphemes;
  } 
  public List<String> posAnnotate(String[] tokens) {
    String[] annotatedText = posTagger.tag(tokens);
    List<String> posTags = new ArrayList<String>(Arrays.asList(annotatedText));
    return posTags;
  }
  
  public List<Morpheme> getMorphemesFromStrings(List<String> posTags, String[] tokens) {
    List<Morpheme> morphemes = new ArrayList<Morpheme>();
    for (int i = 0; i < posTags.size(); i++) {
      String word = tokens[i];
      String tag = posTags.get(i);
      Morpheme morpheme = morphoFactory.createMorpheme(word, tag);
      morphemes.add(morpheme);
    }
    return morphemes;
  }
  
  private final POSModel loadModel(final String lang, final String model) {
    InputStream trainedModelInputStream = null;
    try {
      // Load the model if it's not there yet
      if (!posModels.containsKey(lang)) {
        if (model.equalsIgnoreCase("baseline")) {
          trainedModelInputStream = getBaselineModelStream(lang, model);
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

  private InputStream getBaselineModelStream(final String lang, final String model) {
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
