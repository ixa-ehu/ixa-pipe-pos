package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.postag.MutableTagDictionary;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * Training POS taggers with Apache OpenNLP Machine Learning API.
 * @author ragerri
 * @version 2014-07-07
 */

public abstract class AbstractTrainer implements Trainer {

  /**
   * The language.
   */
  private String lang;
  /**
   * ObjectStream of the training data.
   */
  private ObjectStream<POSSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  private ObjectStream<POSSample> testSamples;
  /**
   * ObjectStream of the automatically created dictionary data, taken from the
   * training data.
   */
  private WordTagSampleStream dictSamples;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  private int beamSize;
  /**
   * Cutoff value to create tag dictionary from training data.
   */
  private int dictCutOff;
  /**
   * posTaggerFactory features need to be implemented by any class extending
   * this one.
   */
  private POSTaggerFactory posTaggerFactory;

  /**
   * Construct an AbstractTrainer.
   * @param alang
   *          the language
   * @param aTrainData
   *          the training data
   * @param aTestData
   *          the test data
   * @param aDictPath
   *          the tag dictionary path
   * @param aDictCutOff
   *          the cutoff to automatically build a tag dictionary
   * @param aBeamsize
   *          the beamsize for decoding
   * @throws IOException
   *           the io exceptions
   */
  public AbstractTrainer(final String alang, final String aTrainData,
      final String aTestData, final String aDictPath, final int aDictCutOff,
      final int aBeamsize) throws IOException {
    this.lang = alang;
    ObjectStream<String> trainStream = InputOutputUtils
        .readInputData(aTrainData);
    trainSamples = new WordTagSampleStream(trainStream);
    ObjectStream<String> testStream = InputOutputUtils.readInputData(aTestData);
    testSamples = new WordTagSampleStream(testStream);
    ObjectStream<String> dictStream = InputOutputUtils
        .readInputData(aTrainData);
    setDictSamples(new WordTagSampleStream(dictStream));
    this.beamSize = aBeamsize;
    this.dictCutOff = aDictCutOff;

  }

  /*
   * (non-Javadoc)
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
      trainedModel = POSTaggerME.train(lang, trainSamples, params,
          getPosTaggerFactory());
      posEvaluator = evaluate(trainedModel, testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + posEvaluator.getWordAccuracy());
    return trainedModel;
  }

  /*
   * (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.pos.train.Trainer#trainCrossEval(java.lang.String,
   * java.lang.String, opennlp.tools.util.TrainingParameters,
   * java.lang.String[])
   */
  public final POSModel trainCrossEval(final String trainData,
      final String devData, final TrainingParameters params,
      final String[] evalRange) {

    // get best parameters from cross evaluation
    List<Integer> bestParams = null;
    try {
      bestParams = crossEval(trainData, devData, params, evalRange);
    } catch (IOException e) {
      System.err.println("IO error while loading training and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    TrainingParameters crossEvalParams = new TrainingParameters();
    crossEvalParams.put(TrainingParameters.ALGORITHM_PARAM, params.algorithm());
    crossEvalParams.put(TrainingParameters.ITERATIONS_PARAM,
        Integer.toString(bestParams.get(0)));
    crossEvalParams.put(TrainingParameters.CUTOFF_PARAM,
        Integer.toString(bestParams.get(1)));

    // use best parameters to train model
    POSModel trainedModel = train(crossEvalParams);
    return trainedModel;
  }

  /**
   * Cross evaluation for Maxent training to obtain the best iteration.
   * @param trainData
   *          the training data
   * @param devData
   *          the development data
   * @param params
   *          the parameters file
   * @param evalRange
   *          the range to perform cross evaluation
   * @return the best parameters
   * @throws IOException
   *           io exception if data not available
   */
  private List<Integer> crossEval(final String trainData, final String devData,
      final TrainingParameters params, final String[] evalRange)
      throws IOException {

    // cross-evaluation
    System.out.println("Cross Evaluation:");
    // lists to store best parameters
    List<List<Integer>> allParams = new ArrayList<List<Integer>>();
    List<Integer> finalParams = new ArrayList<Integer>();

    // F:<iterations,cutoff> Map
    Map<List<Integer>, Double> results = new LinkedHashMap<List<Integer>, Double>();
    // maximum iterations and cutoff
    Integer cutoffParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.CUTOFF_PARAM));
    List<Integer> cutoffList = new ArrayList<Integer>(Collections.nCopies(
        cutoffParam, 0));
    Integer iterParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.ITERATIONS_PARAM));
    List<Integer> iterList = new ArrayList<Integer>(Collections.nCopies(
        iterParam, 0));
    for (int c = 0; c < cutoffList.size() + 1; c++) {
      int start = Integer.valueOf(evalRange[0]);
      int iterRange = Integer.valueOf(evalRange[1]);

      for (int i = start + start; i < iterList.size() + start; i += iterRange) {
        // reading data for training and test
        ObjectStream<String> trainStream = InputOutputUtils
            .readInputData(trainData);
        ObjectStream<String> devStream = InputOutputUtils
            .readInputData(devData);
        ObjectStream<POSSample> aTrainSamples = new WordTagSampleStream(
            trainStream);
        ObjectStream<POSSample> devSamples = new WordTagSampleStream(devStream);
        ObjectStream<String> dictStream = InputOutputUtils
            .readInputData(trainData);
        ObjectStream<POSSample> aDictSamples = new WordTagSampleStream(
            dictStream);
        // dynamic creation of parameters
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(i));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(c));
        System.out.println("Trying with " + i + " iterations...");
        this.createAutomaticDictionary(aDictSamples, dictCutOff);

        // training model
        POSModel trainedModel = POSTaggerME.train(lang, aTrainSamples, params,
            getPosTaggerFactory());
        // evaluate model
        POSEvaluator posTaggerEvaluator = this.evaluate(trainedModel,
            devSamples);
        // TODO calculate sentence accuracy
        double result = posTaggerEvaluator.getWordAccuracy();
        StringBuilder sb = new StringBuilder();
        sb.append("Iterations: ").append(i).append(" cutoff: ").append(c)
            .append(" ").append("Accuracy: ").append(result).append("\n");
        FileUtils.write(new File("pos-results.txt"), sb.toString(), true);
        List<Integer> bestParams = new ArrayList<Integer>();
        bestParams.add(i);
        bestParams.add(c);
        results.put(bestParams, result);
        System.out.println();
        System.out.println("Iterations: " + i + " cutoff: " + c);
        System.out.println(posTaggerEvaluator.getWordAccuracy());
      }
    }
    // print F1 results by iteration
    System.out.println();
    InputOutputUtils.printIterationResults(results);
    InputOutputUtils.getBestIterations(results, allParams);
    finalParams = allParams.get(0);
    System.out.println("Final Params " + finalParams.get(0) + " "
        + finalParams.get(1));
    return finalParams;
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.pos.train.Trainer#evaluate(opennlp.tools.postag.POSModel
   * , opennlp.tools.util.ObjectStream)
   */
  public final POSEvaluator evaluate(final POSModel trainedModel,
      final ObjectStream<POSSample> aTestSamples) {
    POSTagger posTagger = new POSTaggerME(trainedModel, beamSize, 0);
    POSEvaluator posTaggerEvaluator = new POSEvaluator(posTagger);
    try {
      posTaggerEvaluator.evaluate(aTestSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading test set for evaluation!");
      e.printStackTrace();
      System.exit(1);
    }
    return posTaggerEvaluator;
  }

  /**
   * Automatically create a tag dictionary from training data.
   * @param aDictSamples
   *          the dictSamples created from training data
   * @param aDictCutOff
   *          the cutoff to create the dictionary
   */
  protected final void createAutomaticDictionary(
      final ObjectStream<POSSample> aDictSamples, final int aDictCutOff) {
    if (aDictCutOff != -1) {
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
        dictSamples.reset();
      } catch (IOException e) {
        throw new TerminateToolException(-1,
            "IO error while creating/extending POS Dictionary: "
                + e.getMessage(), e);
      }
    }
  }

  /**
   * Create a tag dictionary with the dictionary contained in the dictPath.
   * @param dictPath
   *          the string pointing to the tag dictionary
   */
  protected final void createTagDictionary(final String dictPath) {
    if (dictPath != null) {
      try {
        getPosTaggerFactory().setTagDictionary(
            getPosTaggerFactory().createTagDictionary(new File(dictPath)));
      } catch (IOException e) {
        throw new TerminateToolException(-1,
            "IO error while loading POS Dictionary: " + e.getMessage(), e);
      }
    }
  }

  /**
   * Get the dictSamples to automatically create tag dictionary.
   * @return the WordTagSampleStream dictSamples
   */
  protected final WordTagSampleStream getDictSamples() {
    return dictSamples;
  }

  /**
   * Set the dictSamples to automatically create tag dictionary.
   * @param aDictSamples
   *          the dict samples as a {@code WordTagSampleStream}
   */
  protected final void setDictSamples(final WordTagSampleStream aDictSamples) {
    this.dictSamples = aDictSamples;
  }

  /**
   * Get the posTaggerFactory. Every extension of this class must provide an
   * implementation of the posTaggerFactory.
   * @return the posTaggerFactory
   */
  protected final POSTaggerFactory getPosTaggerFactory() {
    return posTaggerFactory;
  }

  /**
   * Set/implement the posTaggerFactory to be used in the pos tagger training.
   * @param aPosTaggerFactory
   *          the pos tagger factory implemented
   */
  protected final void setPosTaggerFactory(
      final POSTaggerFactory aPosTaggerFactory) {
    this.posTaggerFactory = aPosTaggerFactory;
  }

}
