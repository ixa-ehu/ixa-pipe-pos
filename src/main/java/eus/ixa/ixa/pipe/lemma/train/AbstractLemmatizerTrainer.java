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

package eus.ixa.ixa.pipe.lemma.train;

import java.io.IOException;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import eus.ixa.ixa.pipe.lemma.LemmaSample;
import eus.ixa.ixa.pipe.lemma.LemmaSampleStream;
import eus.ixa.ixa.pipe.lemma.LemmatizerEvaluator;
import eus.ixa.ixa.pipe.lemma.LemmatizerFactory;
import eus.ixa.ixa.pipe.lemma.LemmatizerME;
import eus.ixa.ixa.pipe.lemma.LemmatizerModel;
import eus.ixa.ixa.pipe.pos.train.Flags;
import eus.ixa.ixa.pipe.pos.train.InputOutputUtils;

/**
 * Training a Lemmatizer.
 * 
 * @author ragerri
 * @version 2016-01-28
 */

public abstract class AbstractLemmatizerTrainer implements LemmatizerTrainer {

  /**
   * The language.
   */
  private final String lang;
  /**
   * ObjectStream of the training data.
   */
  private final ObjectStream<LemmaSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  private final ObjectStream<LemmaSample> testSamples;
  /**
   * posTaggerFactory features need to be implemented by any class extending
   * this one.
   */
  private LemmatizerFactory lemmatizerFactory;

  /**
   * Construct an AbstractTrainer. In the params parameter there is information
   * about the language, the featureset, and whether to use pos tag dictionaries
   * or automatically created dictionaries from the training set.
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exceptions
   */
  public AbstractLemmatizerTrainer(final TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    final String trainData = Flags.getDataSet("TrainSet", params);
    final String testData = Flags.getDataSet("TestSet", params);
    final ObjectStream<String> trainStream = InputOutputUtils.readFileIntoMarkableStreamFactory(trainData);
    this.trainSamples = new LemmaSampleStream(trainStream);
    final ObjectStream<String> testStream = InputOutputUtils.readFileIntoMarkableStreamFactory(testData);
    this.testSamples = new LemmaSampleStream(testStream);
  }

  public final LemmatizerModel train(final TrainingParameters params) {
    // features
    if (getLemmatizerFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractLemmatizerTrainer must "
              + " create a LemmatizerFactory features!");
    }
    // training model
    LemmatizerModel trainedModel = null;
    LemmatizerEvaluator lemmatizerEvaluator = null;
    try {
      trainedModel = LemmatizerME.train(this.lang, this.trainSamples, params,
          getLemmatizerFactory());
      final LemmatizerME lemmatizer = new LemmatizerME(trainedModel);
      lemmatizerEvaluator = new LemmatizerEvaluator(lemmatizer);
      lemmatizerEvaluator.evaluate(this.testSamples);
    } catch (final IOException e) {
      System.err.println("IO error while loading training and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + lemmatizerEvaluator.getWordAccuracy());
    return trainedModel;
  }

  /**
   * Get the lemmatizerFactory. Every extension of this class must provide an
   * implementation of the lemmatizerFactory.
   * 
   * @return the lemmatizerFactory
   */
  protected final LemmatizerFactory getLemmatizerFactory() {
    return this.lemmatizerFactory;
  }

  /**
   * Set/implement the lemmatizerFactory to be used in the lemmatizer training.
   * 
   * @param aLemmatizerFactory
   *          the Lemmatizer factory implemented
   */
  protected final void setLemmatizerFactory(
      final LemmatizerFactory aLemmatizerFactory) {
    this.lemmatizerFactory = aLemmatizerFactory;
  }

}
