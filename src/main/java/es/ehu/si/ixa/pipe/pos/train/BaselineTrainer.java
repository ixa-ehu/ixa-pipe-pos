package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

public class BaselineTrainer extends AbstractTrainer {
  
  public BaselineTrainer(String lang, String trainData, String testData, int dictCutOff, int beamsize) throws IOException {
    super(lang, trainData, testData, dictCutOff, beamsize);
    
    posTaggerFactory = new BaselineFactory();
    this.getAutomaticDictionary(dictCutOff);
    
  }

}
