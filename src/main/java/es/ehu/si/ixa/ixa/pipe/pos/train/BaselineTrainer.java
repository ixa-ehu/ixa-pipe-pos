package es.ehu.si.ixa.ixa.pipe.pos.train;

import java.io.IOException;

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
   * @param lang
   *          the language
   * @param trainData
   *          the training data
   * @param testData
   *          the test data
   * @param dictPath
   *          the path to the already created tag dictionary
   * @param dictCutOff
   *          the cutoff to automatically create a tag dictionary from training
   *          data
   * @param beamsize
   *          the beamsize for decoding
   * @throws IOException
   *           the io exception
   */
  public BaselineTrainer(final String lang, final String trainData,
      final String testData, final String dictPath, final int dictCutOff,
      final int beamsize) throws IOException {
    super(lang, trainData, testData, dictPath, dictCutOff, beamsize);

    setPosTaggerFactory(new BaselineFactory());
    this.createTagDictionary(dictPath);
    this.createAutomaticDictionary(getDictSamples(), dictCutOff);
    
  }

}
