package ixa.opennlp.pos;

import java.io.InputStream;

public class Models {

  private InputStream posModel;

  public InputStream getPOSModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      posModel = getClass().getResourceAsStream(
          "/en-pos-perceptron-1000-dev.bin");
    }

    if (cmdOption.equals("es")) {
      posModel = getClass().getResourceAsStream(
          "/en-pos-perceptron-1000-dev.bin");
    }
    return posModel;
  }

}
