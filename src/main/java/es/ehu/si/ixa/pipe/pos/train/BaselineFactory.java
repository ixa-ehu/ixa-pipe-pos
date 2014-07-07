package es.ehu.si.ixa.pipe.pos.train;

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
   * Creates a {@link POSTaggerFactory} that provides the default implementation
   * of the resources.
   */
  public BaselineFactory() {
  }

  /**
   * Creates a {@link POSTaggerFactory}. Use this constructor to
   * programmatically create a factory.
   * 
   * @param ngramDictionary
   * @param posDictionary
   */
  public BaselineFactory(Dictionary ngramDictionary, TagDictionary posDictionary) {
    super(ngramDictionary, posDictionary);
  }

  public POSContextGenerator getPOSContextGenerator() {
    return new BaselineContextGenerator(0, getDictionary());
  }

  public POSContextGenerator getPOSContextGenerator(int cacheSize) {
    return new BaselineContextGenerator(cacheSize, getDictionary());
  }

}
