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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.cmdline.postag.POSEvaluationErrorListener;
import opennlp.tools.cmdline.postag.POSTaggerFineGrainedReportListener;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerEvaluationMonitor;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;
import eus.ixa.ixa.pipe.pos.train.InputOutputUtils;

/**
 * Evaluation class mostly inspired by {@link POSEvaluator}.
 * 
 * @author ragerri
 * @version 2014-07-08
 */
public class Evaluate {

  /**
   * The reference corpus to evaluate against.
   */
  private final ObjectStream<POSSample> testSamples;
  /**
   * Static instance of {@link TokenNameFinderModel}.
   */
  private static POSModel posModel;
  /**
   * An instance of the probabilistic {@link POSTaggerME}.
   */
  private final POSTaggerME posTagger;

  /**
   * Construct an evaluator. The features are encoded in the model itself.
   * 
   * @param testData
   *          the reference data to evaluate against
   * @param model
   *          the model to be evaluated
   * @throws IOException
   *           if input data not available
   */
  public Evaluate(final String testData, final String model) throws IOException {

    final ObjectStream<String> testStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(testData);
    this.testSamples = new WordTagSampleStream(testStream);
    InputStream trainedModelInputStream = null;
    try {
      if (posModel == null) {
        trainedModelInputStream = new FileInputStream(model);
        posModel = new POSModel(trainedModelInputStream);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (final IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
    this.posTagger = new POSTaggerME(posModel);
  }

  /**
   * Evaluate and print word accuracy.
   * @throws IOException
   *           if test corpus not loaded
   */
  public final void evaluate() throws IOException {
    final POSEvaluator evaluator = new POSEvaluator(this.posTagger);
    evaluator.evaluate(this.testSamples);
    System.out.println(evaluator.getWordAccuracy());
  }

  /**
   * Detail evaluation of a model, outputting the report a file.
   * 
   * @throws IOException
   *           the io exception if not output file provided
   */
  public final void detailEvaluate() throws IOException {
    final List<EvaluationMonitor<POSSample>> listeners = new LinkedList<EvaluationMonitor<POSSample>>();
    final POSTaggerFineGrainedReportListener detailedFListener = new POSTaggerFineGrainedReportListener(
        System.out);
    listeners.add(detailedFListener);
    final POSEvaluator evaluator = new POSEvaluator(this.posTagger,
        listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(this.testSamples);
    detailedFListener.writeReport();
  }

  /**
   * Evaluate and print every error.
   * 
   * @throws IOException
   *           if test corpus not loaded
   */
  public final void evalError() throws IOException {
    final List<EvaluationMonitor<POSSample>> listeners = new LinkedList<EvaluationMonitor<POSSample>>();
    listeners.add(new POSEvaluationErrorListener());
    final POSEvaluator evaluator = new POSEvaluator(this.posTagger,
        listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(this.testSamples);
    System.out.println(evaluator.getWordAccuracy());
  }

}
