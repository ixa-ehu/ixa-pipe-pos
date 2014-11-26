package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.postag.POSTaggerFactory;
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
    getTrainerFactory(params);
    this.createTagDictionary(dictPath);
    this.createAutomaticDictionary(getDictSamples(), getDictCutOff());
    
  }
  
  private void getTrainerFactory(TrainingParameters params) {
    String featureSet = Flags.getDictionaryFeatures(params);
    if (featureSet.equalsIgnoreCase("Opennlp")) {
      setPosTaggerFactory(new POSTaggerFactory());
    } else {
      setPosTaggerFactory(new BaselineFactory());
    }
  }

}
