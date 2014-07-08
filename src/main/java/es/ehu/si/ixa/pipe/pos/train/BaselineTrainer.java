package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

public class BaselineTrainer extends AbstractTrainer {
  
  public BaselineTrainer(String lang, String trainData, String testData, String dictPath, int dictCutOff, int beamsize) throws IOException {
    super(lang, trainData, testData, dictPath, dictCutOff, beamsize);
    
    posTaggerFactory = new BaselineFactory();
    this.getAutomaticDictionary(dictSamples, dictCutOff);
    this.createTagDictionary(dictPath);
    
  }

}
