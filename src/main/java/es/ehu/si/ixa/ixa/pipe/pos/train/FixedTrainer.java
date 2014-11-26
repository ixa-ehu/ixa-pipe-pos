package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.TrainingParameters;

/**
 * FixedTrainer to train the pos tagger.
 * @author ragerri
 * @version 2014-11-26
 */
public class FixedTrainer extends AbstractTrainer {

  /**
   * Extends the {@code AbstractTrainer} providing some {@code POSTaggerFactory}.
   *
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exception
   */
  public FixedTrainer(final TrainingParameters params) throws IOException {
    super(params);
    
    String dictPath = Flags.getDictionaryFeatures(params);
    setPosTaggerFactory(getTrainerFactory(params));
    createTagDictionary(dictPath);
    createAutomaticDictionary(getDictSamples(), getDictCutOff());
    
  }
  
  /**
   * Instantiate the {@code POSTaggerFactory} according to the features
   * specified in the parameters properties file.
   * @param params the training parameters
   * @return the factory
   */
  private final POSTaggerFactory getTrainerFactory(TrainingParameters params) {
    POSTaggerFactory posTaggerFactory = null;
    String featureSet = Flags.getDictionaryFeatures(params);
    Dictionary ngramDictionary = null;
    if (Flags.getNgramDictFeatures(params) != Flags.DEFAULT_DICT_CUTOFF) {
      ngramDictionary = createNgramDictionary(getDictSamples(), getNgramDictCutOff());
    }
    if (featureSet.equalsIgnoreCase("Opennlp")) {
      try {
        posTaggerFactory = POSTaggerFactory.create(POSTaggerFactory.class.getName(), ngramDictionary, null);
      } catch (InvalidFormatException e) {
        e.printStackTrace();
      }
    } else {
      try {
        posTaggerFactory = POSTaggerFactory.create(BaselineFactory.class.getName(), ngramDictionary, null);
      } catch (InvalidFormatException e) {
        e.printStackTrace();
      }
    }
    return posTaggerFactory;
  }

}
