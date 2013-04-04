package ixa.opennlp.pos;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 * Simple POS tagging module based on Apache OpenNLP.
 * 
 * Models trained by IXA NLP Group.
 * 
 * @author ragerri 2012/11/30
 * 
 */

public class POS {

  private POSModel posModel;
  private POSTaggerME posTagger;

  /**
   * It constructs an object POS from the POS class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public POS(InputStream trainedModel) {

    // InputStream trainedModel =
    // getClass().getResourceAsStream("/en-pos-perceptron-1000-dev.bin");

    try {
      posModel = new POSModel(trainedModel);

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }
    posTagger = new POSTaggerME(posModel);
  }

  /**
   * This method receives as an input an array of Apache OpenNLP tokenized text
   * and calls the NameFinderME.find(tokens) to recognize and classify Named
   * Entities.
   * 
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an array of OpenNLP Spans of annotated text
   */
  public String[] posAnnotate(String[] tokens) {
    String[] posTags = posTagger.tag(tokens);
    return posTags;
  }

}