package es.ehu.si.ixa.pipe.pos.train;

import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

public interface Trainer {
  
  public POSModel train(TrainingParameters params);
  
  public POSModel trainCrossEval(String trainData,
      String devData, TrainingParameters params, String[] evalRange);

  public POSEvaluator evaluate(POSModel trainedModel,
      ObjectStream<POSSample> testSamples); 


}
