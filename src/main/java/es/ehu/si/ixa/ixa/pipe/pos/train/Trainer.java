package es.ehu.si.ixa.ixa.pipe.pos.train;

import opennlp.tools.postag.POSModel;
import opennlp.tools.util.TrainingParameters;

/**
 * Interface for pos tagger trainers.
 * @author ragerri
 * @version 2014-07-08
 */
public interface Trainer {

  /**
   * Train a pos model with a parameters file.
   * @param params
   *          the parameters file
   * @return the {@code POSModel} trained
   */
  POSModel train(TrainingParameters params);

}
