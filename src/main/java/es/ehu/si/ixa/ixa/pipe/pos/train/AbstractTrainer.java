package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.File;
import java.io.IOException;

import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.postag.MutableTagDictionary;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
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
   * Construct an AbstractTrainer. In the params parameter there is information
   * about the language, the featureset, and whether to use pos tag dictionaries
   * or automatically created dictionaries from the training set.
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exceptions
   */
  public AbstractTrainer(TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    String trainData = Flags.getDataSet("TrainSet", params);
    String testData = Flags.getDataSet("TestSet", params);
    ObjectStream<String> trainStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    trainSamples = new WordTagSampleStream(trainStream);
    ObjectStream<String> testStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(testData);
    testSamples = new WordTagSampleStream(testStream);
    ObjectStream<String> dictStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    setDictSamples(new WordTagSampleStream(dictStream));
    this.beamSize = Flags.getBeamsize(params);
    this.dictCutOff = Flags.getAutoDictFeatures(params);

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
      trainedModel = POSTaggerME.train(lang, trainSamples, params,
          getPosTaggerFactory());
      POSTaggerME posTagger = new POSTaggerME(trainedModel, beamSize, beamSize);
      posEvaluator = new POSEvaluator(posTagger);
      posEvaluator.evaluate(testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + posEvaluator.getWordAccuracy());
    return trainedModel;
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
   * 
   * @param dictPath
   *          the string pointing to the tag dictionary
   */
  protected final void createTagDictionary(final String dictPath) {
    if (!dictPath.equalsIgnoreCase(Flags.DEFAULT_DICT_PATH)) {
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
   * 
   * @return the WordTagSampleStream dictSamples
   */
  protected final WordTagSampleStream getDictSamples() {
    return dictSamples;
  }

  /**
   * Set the dictSamples to automatically create tag dictionary.
   * 
   * @param aDictSamples
   *          the dict samples as a {@code WordTagSampleStream}
   */
  protected final void setDictSamples(final WordTagSampleStream aDictSamples) {
    this.dictSamples = aDictSamples;
  }

  /**
   * Get the posTaggerFactory. Every extension of this class must provide an
   * implementation of the posTaggerFactory.
   * 
   * @return the posTaggerFactory
   */
  protected final POSTaggerFactory getPosTaggerFactory() {
    return posTaggerFactory;
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
    return dictCutOff;
  }

}
