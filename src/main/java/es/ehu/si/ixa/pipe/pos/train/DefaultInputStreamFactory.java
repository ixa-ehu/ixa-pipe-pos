package es.ehu.si.ixa.pipe.pos.train;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.util.InputStreamFactory;

public class DefaultInputStreamFactory implements InputStreamFactory {

  
  private InputStream is;

  public DefaultInputStreamFactory(InputStream is) throws FileNotFoundException {
    this.is = is;
  }

  public InputStream createInputStream() throws IOException {
    return is;
  }
}
