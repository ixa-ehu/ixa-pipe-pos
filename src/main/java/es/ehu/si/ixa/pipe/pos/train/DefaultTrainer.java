package es.ehu.si.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.postag.POSTaggerFactory;

public class DefaultTrainer extends AbstractTrainer {
  
  public DefaultTrainer(String lang, String trainData, String testData, String dictPath, int dictCutOff, int beamsize) throws IOException {
    super(lang, trainData, testData, dictPath, dictCutOff, beamsize);
    
    posTaggerFactory = new POSTaggerFactory();
    this.getAutomaticDictionary(dictSamples, dictCutOff);
    this.createTagDictionary(dictPath);

  }

}
