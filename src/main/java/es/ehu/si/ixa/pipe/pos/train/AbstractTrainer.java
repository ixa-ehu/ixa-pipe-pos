package es.ehu.si.ixa.pipe.pos.train;

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
 * Training POS tagger with Apache OpenNLP Machine Learning API.
 * 
 * @author ragerri
 * @version 2014-07-07
 * 
 */

public abstract class AbstractTrainer implements Trainer {

    protected String lang;
    /**
     * String holding the training data.
     */
    protected String trainData;
    /**
     * String holding the testData.
     */
    protected String testData;
    /**
     * ObjectStream of the training data.
     */
    protected ObjectStream<POSSample> trainSamples;
    /**
     * ObjectStream of the test data.
     */
    protected ObjectStream<POSSample> testSamples;
    /**
     * beamsize value needs to be established in any class extending this one.
     */
    protected int beamSize;
    /**
     * Cutoff value to create tag dictionary from training data.
     */
    private int dictCutOff;
    /**
     * ObjectStream of the dictionary data, taken from the training data.
     */
    private WordTagSampleStream dictSamples;

    /**
     * posTaggerFactory features need to be implemented by any class extending this one.
     */
    protected POSTaggerFactory posTaggerFactory;
   
    public AbstractTrainer(String alang, String aTrainData, String aTestData, int aDictCutOff, int aBeamsize) throws IOException {
      this.lang = alang;
      this.trainData = aTrainData;
      this.testData = aTestData;
      ObjectStream<String> trainStream = InputOutputUtils.readInputData(aTrainData);
      trainSamples = new WordTagSampleStream(trainStream);
      ObjectStream<String> testStream = InputOutputUtils.readInputData(aTestData);
      testSamples = new WordTagSampleStream(testStream);
      ObjectStream<String> dictStream = InputOutputUtils.readInputData(aTrainData);
      dictSamples = new WordTagSampleStream(dictStream);
      this.beamSize = aBeamsize;
      this.dictCutOff = aDictCutOff;
      
    }
    
    public POSModel train(TrainingParameters params) {
      // features
      if (posTaggerFactory == null) {
        throw new IllegalStateException(
        "Classes derived from AbstractTrainer must create a POSTaggerFactory features!");
      }
      this.getAutomaticDictionary(dictSamples, dictCutOff);
      // training model
      POSModel trainedModel = null;
      POSEvaluator posEvaluator = null;    
      try {
        trainedModel = POSTaggerME.train(lang, trainSamples, params,
            posTaggerFactory);
        posEvaluator = evaluate(trainedModel,
            testSamples);
      } catch (IOException e) {
        System.err.println("IO error while loading traing and test sets!");
        e.printStackTrace();
        System.exit(1);
      }
      System.out.println("Final result: " + posEvaluator.getWordAccuracy());
      return trainedModel;
    }

    public POSModel trainCrossEval(String trainData, String devData,
        TrainingParameters params, String[] evalRange) {

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

    private List<Integer> crossEval(String trainData, String devData,
        TrainingParameters params, String[] evalRange)
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
        
        for (int i = start + 10; i < iterList.size() + 10; i += iterRange) {
          // reading data for training and test
          ObjectStream<String> trainStream = InputOutputUtils.readInputData(trainData);
          ObjectStream<String> devStream = InputOutputUtils.readInputData(devData);
          ObjectStream<POSSample> trainSamples = new WordTagSampleStream(
              trainStream);
          ObjectStream<POSSample> devSamples = new WordTagSampleStream(devStream);
          ObjectStream<String> dictStream = InputOutputUtils.readInputData(trainData);
          ObjectStream<POSSample> dictSamples = new WordTagSampleStream(dictStream);
          // dynamic creation of parameters
          params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(i));
          params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(c));
          System.out.println("Trying with " + i + " iterations...");
          this.getAutomaticDictionary(dictSamples, dictCutOff);
          
          // training model
          POSModel trainedModel = POSTaggerME.train(lang, trainSamples, params,
              posTaggerFactory);
          // evaluate model
          POSEvaluator posTaggerEvaluator = this.evaluate(trainedModel,
              devSamples);
          // TODO calculate sentence accuracy 
          double result = posTaggerEvaluator.getWordAccuracy();
          StringBuilder sb = new StringBuilder();
          sb.append("Iterations: ").append(i).append(" cutoff: ").append(c).append(" ")
          .append("Accuracy: ").append(result).append("\n");
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

    public POSEvaluator evaluate(POSModel trainedModel,
        ObjectStream<POSSample> testSamples) {
      POSTagger posTagger = new POSTaggerME(trainedModel, beamSize, 0);
      POSEvaluator posTaggerEvaluator = new POSEvaluator(posTagger);
      try {
        posTaggerEvaluator.evaluate(testSamples);
      } catch (IOException e) {
        System.err.println("IO error while loading test set for evaluation!");
        e.printStackTrace();
        System.exit(1);
      }
      return posTaggerEvaluator;
    }
    
    public void getAutomaticDictionary(ObjectStream<POSSample> dictSamples, int dictCutOff) {
      if (dictCutOff != -1) {
        try {
          TagDictionary dict = posTaggerFactory.getTagDictionary();
          if (dict == null) {
            dict = posTaggerFactory.createEmptyTagDictionary();
            posTaggerFactory.setTagDictionary(dict);
          }
          if (dict instanceof MutableTagDictionary) {
            POSTaggerME.populatePOSDictionary(dictSamples, (MutableTagDictionary)dict,
                dictCutOff);
          } else {
            throw new IllegalArgumentException(
                "Can't extend a POSDictionary that does not implement MutableTagDictionary.");
          }
          dictSamples.reset();
        } catch (IOException e) {
          throw new TerminateToolException(-1,
              "IO error while creating/extending POS Dictionary: "
                  + e.getMessage(), e);
        }
      }
    }
}
