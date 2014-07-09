package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.postag.POSTaggerFactory;

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
   *
   * @param lang
   *          the language
   * @param trainData
   *          the training data
   * @param testData
   *          the test data
   * @param dictPath
   *          the path to the tag dictionary
   * @param dictCutOff
   *          the cutoff to automatically build a dictionary from training data
   * @param beamsize
   *          the beamsize for decoding
   * @throws IOException
   *           the io exception for input data
   */
  public DefaultTrainer(final String lang, final String trainData,
      final String testData, final String dictPath, final int dictCutOff,
      final int beamsize) throws IOException {
    super(lang, trainData, testData, dictPath, dictCutOff, beamsize);

    setPosTaggerFactory(new POSTaggerFactory());
    this.createAutomaticDictionary(getDictSamples(), dictCutOff);
    this.createTagDictionary(dictPath);

  }

}
