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
package eus.ixa.ixa.pipe.pos.eval;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.cmdline.postag.POSEvaluationErrorListener;
import opennlp.tools.cmdline.postag.POSTaggerFineGrainedReportListener;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerCrossValidator;
import opennlp.tools.postag.POSTaggerEvaluationMonitor;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.EvaluationMonitor;
import eus.ixa.ixa.pipe.pos.train.BaselineFactory;
import eus.ixa.ixa.pipe.pos.train.Flags;
import eus.ixa.ixa.pipe.pos.train.InputOutputUtils;

/**
 * Training POS tagger with Apache OpenNLP Machine Learning API via cross
 * validation.
 * 
 * @author ragerri
 * @version 2014-11-25
 */

public class CrossValidator {

  /**
   * The language.
   */
  private String lang;
  /**
   * ObjectStream of the training data.
   */
  private ObjectStream<POSSample> trainSamples;
  /**
   * Cutoff value to create tag dictionary from training data.
   */
  private int dictCutOff;
  /**
   * The folds value for cross validation.
   */
  private int folds;
  /**
   * posTaggerFactory features need to be implemented by any class extending
   * this one.
   */
  private POSTaggerFactory posTaggerFactory;
  /**
   * The evaluation listeners.
   */
  private List<EvaluationMonitor<POSSample>> listeners = new LinkedList<EvaluationMonitor<POSSample>>();
  POSTaggerFineGrainedReportListener detailedListener;

  /**
   * Construct a CrossValidator. In the params parameter there is information
   * about the language, the featureset, and whether to use pos tag dictionaries
   * or automatically created dictionaries from the training set.
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exceptions
   */
  public CrossValidator(TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    String trainData = Flags.getDataSet("TrainSet", params);
    ObjectStream<String> trainStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    trainSamples = new WordTagSampleStream(trainStream);
    this.dictCutOff = Flags.getAutoDictFeatures(params);
    this.folds = Flags.getFolds(params);
    createPOSFactory(params);
    getEvalListeners(params);
  }

  private void createPOSFactory(TrainingParameters params) {
    String featureSet = Flags.getFeatureSet(params);
    if (featureSet.equalsIgnoreCase("Opennlp")) {
      posTaggerFactory = new POSTaggerFactory();
    } else {
      posTaggerFactory = new BaselineFactory();
    }
  }

  private void getEvalListeners(TrainingParameters params) {
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("error")) {
      listeners.add(new POSEvaluationErrorListener());
    }
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("detailed")) {
      detailedListener = new POSTaggerFineGrainedReportListener();
      listeners.add(detailedListener);
    }
  }

  /**
   * Cross validate when no separate testset is available.
   * @param params the training parameters
   */
  public final void crossValidate(final TrainingParameters params) {

    POSTaggerCrossValidator validator = null;
    try {
      validator = getPOSTaggerCrossValidator(params);
      validator.evaluate(trainSamples, folds);
    } catch (IOException e) {
      System.err.println("IO error while loading training set!");
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        trainSamples.close();
      } catch (IOException e) {
        System.err.println("IO error with the train samples!");
      }
    }
    if (detailedListener == null) {
      System.out.println(validator.getWordAccuracy());
    } else {
      //TODO add detailed evaluation here
      System.out.println(validator.getWordAccuracy());
    }
  }

  /**
   * Get the postagger cross validator.
   * @param params the training parameters
   * @return the pos tagger cross validator
   */
  private POSTaggerCrossValidator getPOSTaggerCrossValidator(
      TrainingParameters params) {
    File dictPath = new File(Flags.getDictionaryFeatures(params));
    // features
    if (posTaggerFactory == null) {
      throw new IllegalStateException(
          "You must create the POSTaggerFactory features!");
    }
    POSTaggerCrossValidator validator = null;
    if (dictPath.getName().equals(Flags.DEFAULT_DICT_PATH)) {
      if (this.dictCutOff == Flags.DEFAULT_DICT_CUTOFF) {
        validator = new POSTaggerCrossValidator(lang, params, null, null,
            null, posTaggerFactory.getClass().getName(),
            listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      } else {
        validator = new POSTaggerCrossValidator(lang, params, null, null,
            dictCutOff, posTaggerFactory.getClass().getName(),
            listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      } 
    } else {
      if (this.dictCutOff == Flags.DEFAULT_DICT_CUTOFF) {
        validator = new POSTaggerCrossValidator(lang, params, dictPath, null,
            null, posTaggerFactory.getClass().getName(),
            listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      } else {
        validator = new POSTaggerCrossValidator(lang, params, dictPath, null,
            dictCutOff, posTaggerFactory.getClass().getName(),
            listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      }
    }
    return validator;
  }

}
