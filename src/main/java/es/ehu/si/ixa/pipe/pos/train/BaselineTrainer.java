package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

public class BaselineTrainer extends AbstractTrainer {
  
  public BaselineTrainer(String lang, String trainData, String testData, int beamsize) throws IOException {
    super(lang, trainData, testData, beamsize);
    
    posTaggerFactory = new BaselineFactory();
  }

}
