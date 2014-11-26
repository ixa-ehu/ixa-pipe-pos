package es.ehu.si.ixa.ixa.pipe.pos.train;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSContextGenerator;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.TagDictionary;

/**
 * Extends the POSTagger Factory. Right now we only override the context
 * generators.
 */
public class BaselineFactory extends POSTaggerFactory {

  /**
   * Creates a {@link BaselineFactory} that provides the default implementation
   * of the resources.
   */
  public BaselineFactory() {
  }

  /**
   * Creates a {@link POSTaggerFactory}. Use this constructor to
   * programmatically create a factory.
   *
   * @param ngramDictionary
   *          the ngrams dictionary
   * @param posDictionary
   *          the postags dictionary
   */
  public BaselineFactory(final Dictionary ngramDictionary,
      final TagDictionary posDictionary) {
    super(ngramDictionary, posDictionary);
  }

  /*
   * (non-Javadoc)
   *
   * @see opennlp.tools.postag.POSTaggerFactory#getPOSContextGenerator()
   */
  @Override
  public final POSContextGenerator getPOSContextGenerator() {
    return new BaselineContextGenerator(0, getDictionary());
  }

  /*
   * (non-Javadoc)
   *
   * @see opennlp.tools.postag.POSTaggerFactory#getPOSContextGenerator(int)
   */
  @Override
  public final POSContextGenerator getPOSContextGenerator(final int cacheSize) {
    return new BaselineContextGenerator(cacheSize, getDictionary());
  }

}
