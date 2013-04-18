/*
 * Copyright 2013 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package ixa.pipe.pos;

import java.io.InputStream;
import java.net.URL;

public class Resources {

  private InputStream posModel;
  private InputStream dict;
  private URL dictURL;
  private String constantTag;
  

  public InputStream getPOSModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      posModel = getClass().getResourceAsStream(
          "/en-pos-perceptron-500-dev.bin");
    }

    if (cmdOption.equals("es")) {
      posModel = getClass().getResourceAsStream(
          "/en-pos-perceptron-500-dev.bin");
    }
    return posModel;
  }
  
  public InputStream getDictionary(String cmdOption) { 
	  if (cmdOption.equalsIgnoreCase("en")) { 
		  dict = getClass().getResourceAsStream("/en-lemmas.dict");
	  }
	  
	  if (cmdOption.equalsIgnoreCase("es")) { 
		  dict = getClass().getResourceAsStream("/es-lemmas.dict");
	  }
	  return dict;
  }
  
  public URL getBinaryDict(String cmdOption) {
	  if (cmdOption.equalsIgnoreCase("en")) { 
		  dictURL = getClass().getResource("/english.dict");  
  }

	  if (cmdOption.equalsIgnoreCase("es")) { 
		  dictURL = getClass().getResource("/spanish.dict");
	  }
	  return dictURL;
  }
  
  public String setTagConstant(String lang, String postag) {
    if (lang.equalsIgnoreCase("en")) {
      if (postag.equalsIgnoreCase("NNP")) { 
        constantTag = "NNP";
      }
    }
    if (lang.equalsIgnoreCase("es")) {
      if (postag.equalsIgnoreCase("NP")){
        constantTag = "NP";
      }
    }
    return constantTag;
  }
}
