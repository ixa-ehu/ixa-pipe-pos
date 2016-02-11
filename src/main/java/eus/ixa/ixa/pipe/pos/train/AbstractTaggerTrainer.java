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
package eus.ixa.ixa.pipe.pos.train;

import java.io.File;
import java.io.IOException;

import eus.ixa.ixa.pipe.pos.MorphoSampleStream;

import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.MutableTagDictionary;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * Training POS tagger with Apache OpenNLP Machine Learning API.
 * 
 * @author ragerri
 * @version 2014-07-07
 */

public abstract class AbstractTaggerTrainer implements TaggerTrainer {

  /**
   * The language.
   */
  private final String lang;
  /**
   * ObjectStream of the training data.
   */
  private final ObjectStream<POSSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  private final ObjectStream<POSSample> testSamples;
  /**
   * ObjectStream of the automatically created dictionary data, taken from the
   * training data.
   */
  private MorphoSampleStream dictSamples;
  /**
   * Cutoff value to create tag dictionary from training data.
   */
  private final int dictCutOff;
  /**
   * Cutoff value to create tag dictionary from training data.
   */
  private final int ngramCutOff;
  /**
   * posTaggerFactory features need to be implemented by any class extending
   * this one.
   */
  private POSTaggerFactory posTaggerFactory;

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
  public AbstractTaggerTrainer(final TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    final String trainData = Flags.getDataSet("TrainSet", params);
    final String testData = Flags.getDataSet("TestSet", params);
    final ObjectStream<String> trainStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    this.trainSamples = new MorphoSampleStream(trainStream);
    final ObjectStream<String> testStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(testData);
    this.testSamples = new MorphoSampleStream(testStream);
    final ObjectStream<String> dictStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    setDictSamples(new MorphoSampleStream(dictStream));
    this.dictCutOff = Flags.getAutoDictFeatures(params);
    this.ngramCutOff = Flags.getNgramDictFeatures(params);

  }

  /*
   * (non-Javadoc)
   * 
   * @see es.ehu.si.ixa.pipe.pos.train.Trainer#train(opennlp.tools.util.
   * TrainingParameters)
   */
  public final POSModel train(final TrainingParameters params) {
    // features
    if (getPosTaggerFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractTrainer must "
              + " create a POSTaggerFactory features!");
    }
    // training model
    POSModel trainedModel = null;
    POSEvaluator posEvaluator = null;
    try {
      trainedModel = POSTaggerME.train(this.lang, this.trainSamples, params,
          getPosTaggerFactory());
      final POSTaggerME posTagger = new POSTaggerME(trainedModel);
      posEvaluator = new POSEvaluator(posTagger);
      posEvaluator.evaluate(this.testSamples);
    } catch (final IOException e) {
      System.err.println("IO error while loading training and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + posEvaluator.getWordAccuracy());
    return trainedModel;
  }

  /**
   * Create a tag dictionary with the dictionary contained in the dictPath.
   * 
   * @param dictPath
   *          the string pointing to the tag dictionary
   */
  protected final void createTagDictionary(final String dictPath) {
    if (!dictPath.equalsIgnoreCase(Flags.DEFAULT_DICT_PATH)) {
      try {
        getPosTaggerFactory().setTagDictionary(
            getPosTaggerFactory().createTagDictionary(new File(dictPath)));
      } catch (final IOException e) {
        throw new TerminateToolException(-1,
            "IO error while loading POS Dictionary: " + e.getMessage(), e);
      }
    }
  }

  /**
   * Automatically create a tag dictionary from training data.
   * 
   * @param aDictSamples
   *          the dictSamples created from training data
   * @param aDictCutOff
   *          the cutoff to create the dictionary
   */
  protected final void createAutomaticDictionary(
      final ObjectStream<POSSample> aDictSamples, final int aDictCutOff) {
    if (aDictCutOff != Flags.DEFAULT_DICT_CUTOFF) {
      try {
        TagDictionary dict = getPosTaggerFactory().getTagDictionary();
        if (dict == null) {
          dict = getPosTaggerFactory().createEmptyTagDictionary();
          getPosTaggerFactory().setTagDictionary(dict);
        }
        if (dict instanceof MutableTagDictionary) {
          POSTaggerME.populatePOSDictionary(aDictSamples,
              (MutableTagDictionary) dict, aDictCutOff);
        } else {
          throw new IllegalArgumentException("Can't extend a POSDictionary"
              + " that does not implement MutableTagDictionary.");
        }
        this.dictSamples.reset();
      } catch (final IOException e) {
        throw new TerminateToolException(-1,
            "IO error while creating/extending POS Dictionary: "
                + e.getMessage(), e);
      }
    }
  }

  /**
   * Create ngram dictionary from training data.
   * 
   * @param aDictSamples
   *          the training data
   * @param aNgramCutoff
   *          the cutoff
   * @return ngram dictionary
   */
  protected final Dictionary createNgramDictionary(
      final ObjectStream<POSSample> aDictSamples, final int aNgramCutoff) {
    Dictionary ngramDict = null;
    if (aNgramCutoff != Flags.DEFAULT_DICT_CUTOFF) {
      System.err.print("Building ngram dictionary ... ");
      try {
        ngramDict = POSTaggerME
            .buildNGramDictionary(aDictSamples, aNgramCutoff);
        this.dictSamples.reset();
      } catch (final IOException e) {
        throw new TerminateToolException(-1,
            "IO error while building NGram Dictionary: " + e.getMessage(), e);
      }
      System.err.println("done");
    }
    return ngramDict;
  }

  /**
   * Get the dictSamples to automatically create tag dictionary.
   * 
   * @return the MorphoSampleStream dictSamples
   */
  protected final MorphoSampleStream getDictSamples() {
    return this.dictSamples;
  }

  /**
   * Set the dictSamples to automatically create tag dictionary.
   * 
   * @param aDictSamples
   *          the dict samples as a {@code MorphoSampleStream}
   */
  protected final void setDictSamples(final MorphoSampleStream aDictSamples) {
    this.dictSamples = aDictSamples;
  }

  /**
   * Get the posTaggerFactory. Every extension of this class must provide an
   * implementation of the posTaggerFactory.
   * 
   * @return the posTaggerFactory
   */
  protected final POSTaggerFactory getPosTaggerFactory() {
    return this.posTaggerFactory;
  }

  /**
   * Set/implement the posTaggerFactory to be used in the pos tagger training.
   * 
   * @param aPosTaggerFactory
   *          the pos tagger factory implemented
   */
  protected final void setPosTaggerFactory(
      final POSTaggerFactory aPosTaggerFactory) {
    this.posTaggerFactory = aPosTaggerFactory;
  }

  /**
   * Get the cutoff to create automatic dictionary from training data.
   * 
   * @return the cutoff
   */
  protected final Integer getDictCutOff() {
    return this.dictCutOff;
  }

  /**
   * Get the cutoff to create automatic ngram dictionary from training data.
   * 
   * @return the cutoff
   */
  protected final Integer getNgramDictCutOff() {
    return this.ngramCutOff;
  }

}
