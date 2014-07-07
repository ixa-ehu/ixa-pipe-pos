package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

public class BaselineMorphoTaggerTrainer extends AbstractMorphoTaggerTrainer {
  
  public BaselineMorphoTaggerTrainer(String lang, String trainData, String testData, int beamsize) throws IOException {
    super(lang, trainData, testData, beamsize);
    //TODO this is just the default!
    posTaggerFactory = new BaselinePOSFactory();
  }

}
