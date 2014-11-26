package es.ehu.si.ixa.ixa.pipe.pos.eval;

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
import es.ehu.si.ixa.ixa.pipe.pos.train.BaselineFactory;
import es.ehu.si.ixa.ixa.pipe.pos.train.Flags;
import es.ehu.si.ixa.ixa.pipe.pos.train.InputOutputUtils;

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
      setPosTaggerFactory(new POSTaggerFactory());
    }
    else {
      setPosTaggerFactory(new BaselineFactory());
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

  //TODO add ngram dictionary parameter
  public final void crossValidate(final TrainingParameters params) {
    File dictPath = new File(Flags.getDictionaryFeatures(params));
    // features
    if (posTaggerFactory == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractTrainer must "
              + " create a POSTaggerFactory features!");
    }
    POSTaggerCrossValidator validator = null;
    try {
      validator = new POSTaggerCrossValidator(lang, params, dictPath, null, dictCutOff, posTaggerFactory.getClass().getName(), listeners.toArray(new POSTaggerEvaluationMonitor[listeners.size()]));
      validator.evaluate(trainSamples, folds);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
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
      System.out.println(detailedListener.toString());
    }
  }

  /**
   * Set/implement the posTaggerFactory to be used in the pos tagger training.
   * 
   * @param aPosTaggerFactory
   *          the pos tagger factory implemented
   */
  private final void setPosTaggerFactory(
      final POSTaggerFactory aPosTaggerFactory) {
    this.posTaggerFactory = aPosTaggerFactory;
  }

}
