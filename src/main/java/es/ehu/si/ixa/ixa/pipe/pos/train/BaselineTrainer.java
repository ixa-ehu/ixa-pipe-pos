package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.util.TrainingParameters;

/**
 * Trainer using the {@code BaselineFactory} to train the pos tagger.
 * @author ragerri
 * @version 2014-07-08
 */
public class BaselineTrainer extends AbstractTrainer {

  /**
   * Extends the {@code AbstractTrainer} providing the {@code BaselineFactory}
   * posTaggerFactory.
   *
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exception
   */
  public BaselineTrainer(final TrainingParameters params) throws IOException {
    super(params);
    
    String dictPath = Flags.getDictionaryFeatures(params);
    setPosTaggerFactory(new BaselineFactory());
    this.createTagDictionary(dictPath);
    this.createAutomaticDictionary(getDictSamples(), getDictCutOff());
    
  }

}
