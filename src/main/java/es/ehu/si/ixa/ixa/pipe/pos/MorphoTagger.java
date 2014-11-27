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

package es.ehu.si.ixa.ixa.pipe.pos;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
   * Construct a morphotagger.
   * @param props the properties object
   */
  public MorphoTagger(final Properties props) {
    String lang = props.getProperty("language");
    String model = props.getProperty("model");
    int beamSize = Integer.parseInt(props.getProperty("beamSize"));
    POSModel posModel = loadModel(lang, model);
    posTagger = new POSTaggerME(posModel, beamSize, beamSize);
  }

  /**
   * Construct a morphotagger with {@code MorphoFactory}.
   * @param props the properties object
   * @param aMorphoFactory the morpho factory
   */
  public MorphoTagger(final Properties props, final MorphoFactory aMorphoFactory) {
    String lang = props.getProperty("language");
    String model = props.getProperty("model");
    int beamSize = Integer.parseInt(props.getProperty("beamSize"));
    POSModel posModel = loadModel(lang, model);
    posTagger = new POSTaggerME(posModel, beamSize, beamSize);
    this.morphoFactory = aMorphoFactory;
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
   * Loads statically the probabilistic model. Every instance of this finder
   * will share the same model.
   *
   * @param lang the language
   * @param model the model to be loaded
   * @return the model as a {@link POSModel} object
   */
  private final POSModel loadModel(final String lang, final String model) {
    long lStartTime = new Date().getTime();
    try {
      posModels.putIfAbsent(lang, new POSModel(new FileInputStream(model)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    long lEndTime = new Date().getTime();
    long difference = lEndTime - lStartTime;
    System.err.println("ixa-pipe-pos model loaded in: " + difference
        + " miliseconds ... [DONE]");
    return posModels.get(lang);
  }

}
