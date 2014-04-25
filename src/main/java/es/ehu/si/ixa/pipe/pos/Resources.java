/*
 * Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.pipe.pos;

import java.io.InputStream;
import java.net.URL;

public class Resources {

  private InputStream dict;
  private URL dictURL;
  private String constantTag;

  public InputStream getDictionary(String lang) {
	  if (lang.equalsIgnoreCase("en")) {
		  dict = getClass().getResourceAsStream("/en-lemmas.dict");
	  }

	  if (lang.equalsIgnoreCase("es")) {
		  dict = getClass().getResourceAsStream("/es-lemmas.dict");
	  }
	  return dict;
  }

  public URL getBinaryDict(String lang) {
	  if (lang.equalsIgnoreCase("en")) {
		  dictURL = getClass().getResource("/english.dict");
  }

	  if (lang.equalsIgnoreCase("es")) {
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
      if (postag.startsWith("NP")){
        constantTag = "NP00000";
      }
    }
    return constantTag;
  }
}
