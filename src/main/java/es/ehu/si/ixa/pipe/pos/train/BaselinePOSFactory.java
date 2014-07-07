package es.ehu.si.ixa.pipe.pos.train;

import opennlp.tools.postag.POSContextGenerator;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.InvalidFormatException;

public class BaselinePOSFactory extends POSTaggerFactory {
  
  public BaselinePOSFactory() {
    
  }
  
  public POSContextGenerator getPOSContextGenerator() {
    return new BaselinePOSContextGenerator(0);
  }
  
  public POSContextGenerator getPOSContextGenerator(int cacheSize) {
    return new BaselinePOSContextGenerator(cacheSize);
  }

  @Override
  public void validateArtifactMap() throws InvalidFormatException {
    // TODO Auto-generated method stub
    
  }

}
