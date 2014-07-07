package es.ehu.si.ixa.pipe.pos.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import es.ehu.si.ixa.pipe.pos.train.InputOutputUtils;
import es.ehu.si.ixa.pipe.pos.train.Trainer;
import opennlp.tools.cmdline.postag.POSEvaluationErrorListener;
import opennlp.tools.cmdline.postag.POSTaggerFineGrainedReportListener;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerEvaluationMonitor;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;

  /**
   * Evaluation class mostly using {@link POSEvaluator}.
   *
   * @author ragerri
   * @version 2013-04-25
   */
  public class Evaluate {

    /**
     * The reference corpus to evaluate against.
     */
    private ObjectStream<POSSample> testSamples;
    /**
     * Static instance of {@link TokenNameFinderModel}.
     */
    private static POSModel posModel;
    /**
     *
     * The name finder trainer to use for appropriate features.
     */
    //TODO deal with features properly, here and in other classes
    private Trainer nameFinderTrainer;
    /**
     * An instance of the probabilistic {@link POSTaggerME}.
     */
    private POSTaggerME posTagger;

    /**
     * Construct an evaluator.
     *
     * @param testData the reference data to evaluate against
     * @param model the model to be evaluated
     * @param features the features
     * @param lang the language
     * @param beamsize the beam size for decoding
     * @throws IOException if input data not available
     */
    public Evaluate( final String lang, final String testData, final String model, final String features,
        final int beamsize) throws IOException {

      ObjectStream<String> testStream = InputOutputUtils.readInputData(testData);
      testSamples = new WordTagSampleStream(testStream);
      InputStream trainedModelInputStream = null;
      try {
        if (posModel == null) {
          trainedModelInputStream = new FileInputStream(model);
          posModel = new POSModel(trainedModelInputStream);
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
      posTagger = new POSTaggerME(posModel, beamsize, 0);
    }

    /**
     * Evaluate and print precision, recall and F measure.
     * @throws IOException if test corpus not loaded
     */
    public final void evaluate() throws IOException {
      POSEvaluator evaluator = new POSEvaluator(posTagger);
      evaluator.evaluate(testSamples);
      System.out.println(evaluator.getWordAccuracy());
    }
    /**
     * Evaluate and print the precision, recall and F measure per
     * named entity class.
     *
     * @throws IOException if test corpus not loaded
     */
    public final void detailEvaluate(String outputFile) throws IOException {
      File reportFile = new File(outputFile);
      OutputStream reportOutputStream = new FileOutputStream(reportFile);
      List<EvaluationMonitor<POSSample>> listeners = new LinkedList<EvaluationMonitor<POSSample>>();
      POSTaggerFineGrainedReportListener detailedFListener = new POSTaggerFineGrainedReportListener(reportOutputStream);
      listeners.add(detailedFListener);
      POSEvaluator evaluator = new POSEvaluator(posTagger,
          listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      evaluator.evaluate(testSamples);
      detailedFListener.writeReport();
      reportOutputStream.close();
    }
    /**
     * Evaluate and print every error.
     * @throws IOException if test corpus not loaded
     */
    public final void evalError() throws IOException {
      List<EvaluationMonitor<POSSample>> listeners = new LinkedList<EvaluationMonitor<POSSample>>();
      listeners.add(new POSEvaluationErrorListener());
      POSEvaluator evaluator = new POSEvaluator(posTagger,
          listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      evaluator.evaluate(testSamples);
      System.out.println(evaluator.getWordAccuracy());
    }


}
