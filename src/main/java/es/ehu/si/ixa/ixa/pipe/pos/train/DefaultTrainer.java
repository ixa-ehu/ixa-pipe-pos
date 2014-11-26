package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.TrainingParameters;

/**
 * Default OpenNLP feature training, kept for upstream compatibility.
 *
 * @author ragerri
 * @version 2014-07-08
 */
public class DefaultTrainer extends AbstractTrainer {

  /**
   * Construct a default opennlp trainer using the default
   * {@code POSTaggerFactory}.
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exception for input data
   */
  public DefaultTrainer(final TrainingParameters params) throws IOException {
    super(params);
    
    String dictPath = Flags.getDictionaryFeatures(params);
    setPosTaggerFactory(new POSTaggerFactory());
    this.createTagDictionary(dictPath);
    this.createAutomaticDictionary(getDictSamples(), getDictCutOff());
  }

}
