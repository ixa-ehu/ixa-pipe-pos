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

package es.ehu.si.ixa.pipe.lemmatize;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import es.ehu.si.ixa.pipe.pos.Resources;

/**
 * 
 * @author ragerri
 * 
 */
public class SimpleLemmatizer implements DictionaryLemmatizer {
  
	private HashMap<List<String>,String> dictMap;

  public SimpleLemmatizer(InputStream dictionary) {
		dictMap = new HashMap<List<String>,String>();
		BufferedReader breader = new BufferedReader(new InputStreamReader(dictionary));
		String line;
	    try {
			while ((line = breader.readLine()) != null) {
				String[] elems = line.split("\t");
				dictMap.put(Arrays.asList(elems[0],elems[2]),elems[1]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
  }
  
  Resources tagRetriever = new Resources();
  
  private List<String> getDictKeys(String lang, String word, String postag) {
    String constantTag = tagRetriever.setTagConstant(lang, postag);
		List<String> keys = new ArrayList<String>();
		if (postag.startsWith(String.valueOf(constantTag))) { 
			keys.addAll(Arrays.asList(word,postag));
		}
		else {
			keys.addAll(Arrays.asList(word.toLowerCase(),postag));
		}
		return keys;
	}
     
  public String lemmatize(String lang, String word, String postag) {
	String lemma = null;
	String constantTag = tagRetriever.setTagConstant(lang, postag);
	List<String> keys = this.getDictKeys(lang, word, postag);
	//lookup lemma as value of the map
	String keyValue = dictMap.get(keys);
	if (keyValue != null) { 
		lemma = keyValue;
	}
	else if (keyValue == null && postag.startsWith(String.valueOf(constantTag))) { 
		lemma = word;
	}
	else if (keyValue == null && word.toUpperCase() == word) { 
		lemma = word;
	}
	else {
		lemma = word.toLowerCase();
	}
	return lemma;  
  }
}