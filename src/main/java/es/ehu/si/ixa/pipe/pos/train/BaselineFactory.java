package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSContextGenerator;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ext.ExtensionLoader;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.UncloseableInputStream;

/**
 * Extends the POSTagger Factory. Right now we only change the context generator.
 */
public class BaselineFactory extends POSTaggerFactory {

  protected Dictionary ngramDictionary;
  protected TagDictionary posDictionary;

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
  public BaselineFactory(Dictionary ngramDictionary,
      TagDictionary posDictionary) {
    super(ngramDictionary, posDictionary);
  }

  public POSContextGenerator getPOSContextGenerator() {
    return new BaselineContextGenerator(0, getDictionary());
  }
  
  public POSContextGenerator getPOSContextGenerator(int cacheSize) {
    return new BaselineContextGenerator(cacheSize, getDictionary());
  }
  
  static class POSDictionarySerializer implements ArtifactSerializer<POSDictionary> {

    public POSDictionary create(InputStream in) throws IOException,
        InvalidFormatException {
      return POSDictionary.create(new UncloseableInputStream(in));
    }

    public void serialize(POSDictionary artifact, OutputStream out)
        throws IOException {
      artifact.serialize(out);
    }

    @SuppressWarnings("rawtypes")
    static void register(Map<String, ArtifactSerializer> factories) {
      factories.put("tagdict", new POSDictionarySerializer());
    }
  }

  public static BaselineFactory create(String subclassName,
      Dictionary ngramDictionary, TagDictionary posDictionary)
      throws InvalidFormatException {
    if (subclassName == null) {
      // will create the default factory
      return new BaselineFactory(ngramDictionary, posDictionary);
    }
    try {
      BaselineFactory theFactory = ExtensionLoader.instantiateExtension(
          BaselineFactory.class, subclassName);
      theFactory.init(ngramDictionary, posDictionary);
      return theFactory;
    } catch (Exception e) {
      String msg = "Could not instantiate the " + subclassName
          + ". The initialization throw an exception.";
      System.err.println(msg);
      e.printStackTrace();
      throw new InvalidFormatException(msg, e);
    }

  }
  
}
