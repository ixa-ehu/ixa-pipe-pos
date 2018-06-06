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

package eus.ixa.ixa.pipe.lemma.eval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.util.ObjectStream;
import eus.ixa.ixa.pipe.lemma.LemmaSample;
import eus.ixa.ixa.pipe.lemma.LemmaSampleStream;
import eus.ixa.ixa.pipe.lemma.LemmatizerEvaluator;
import eus.ixa.ixa.pipe.lemma.LemmatizerME;
import eus.ixa.ixa.pipe.lemma.LemmatizerModel;
import eus.ixa.ixa.pipe.pos.eval.Evaluate;
import eus.ixa.ixa.pipe.pos.train.InputOutputUtils;

/**
 * Evaluation class.
 * 
 * @author ragerri
 * @version 2014-07-08
 */
public class LemmaEvaluate implements Evaluate {

  /**
   * The reference corpus to evaluate against.
   */
  private final ObjectStream<LemmaSample> testSamples;
  /**
   * Static instance of {@link LemmatizerModel}.
   */
  private static LemmatizerModel lemmatizerModel;
  /**
   * An instance of the probabilistic {@link LemmatizerME}.
   */
  private final LemmatizerME lemmatizer;

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
  public LemmaEvaluate(final String testData, final String model) throws IOException {

    final ObjectStream<String> testStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(testData);
    this.testSamples = new LemmaSampleStream(testStream);
    InputStream trainedModelInputStream = null;
    try {
      if (lemmatizerModel == null) {
        trainedModelInputStream = new FileInputStream(model);
        lemmatizerModel = new LemmatizerModel(trainedModelInputStream);
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
    this.lemmatizer = new LemmatizerME(lemmatizerModel);
  }

  /**
   * Evaluate and print word accuracy.
   */
  public final void evaluate() {
    final LemmatizerEvaluator evaluator = new LemmatizerEvaluator(this.lemmatizer);
    try {
      evaluator.evaluate(this.testSamples);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(evaluator.getWordAccuracy());
  }

  @Override
  public void detailEvaluate() {
    // TODO Auto-generated method stub
    final LemmatizerEvaluator evaluator = new LemmatizerEvaluator(this.lemmatizer);
    try {
      evaluator.evaluate(this.testSamples);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(evaluator.getWordAccuracy());
    
  }

  @Override
  public void evalError() {
    // TODO Auto-generated method stub
    final LemmatizerEvaluator evaluator = new LemmatizerEvaluator(this.lemmatizer);
    try {
      evaluator.evaluate(this.testSamples);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(evaluator.getWordAccuracy());
    
  }

  /**
   * Detail evaluation of a model, outputting the report a file.
   * 
   * @throws IOException
   *           the io exception if not output file provided
   *//*
  public final void detailEvaluate() throws IOException {
    final List<EvaluationMonitor<LemmaSample>> listeners = new LinkedList<EvaluationMonitor<LemmaSample>>();
    final LemmatizerFineGrainedReportListener detailedFListener = new LemmatizerFineGrainedReportListener(
        System.out);
    listeners.add(detailedFListener);
    final LemmatizerEvaluator evaluator = new LemmatizerEvaluator(this.lemmatizer,
        listeners.toArray(new LemmatizerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(this.testSamples);
    detailedFListener.writeReport();
  }

  *//**
   * Evaluate and print every error.
   * 
   * @throws IOException
   *           if test corpus not loaded
   *//*
  public final void evalError() throws IOException {
    final List<EvaluationMonitor<LemmaSample>> listeners = new LinkedList<EvaluationMonitor<LemmaSample>>();
    listeners.add(new LemmatizerEvaluationErrorListener());
    final LemmatizerEvaluator evaluator = new LemmatizerEvaluator(this.lemmatizer,
        listeners.toArray(new LemmatizerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(this.testSamples);
    System.out.println(evaluator.getWordAccuracy());
  }
*/
}
