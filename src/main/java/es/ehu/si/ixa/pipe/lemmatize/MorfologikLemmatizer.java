package es.ehu.si.ixa.pipe.lemmatize;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import es.ehu.si.ixa.pipe.pos.Resources;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

public class MorfologikLemmatizer implements es.ehu.si.ixa.pipe.lemmatize.DictionaryLemmatizer {

  private IStemmer dictLookup;

  public MorfologikLemmatizer(URL dictURL) throws IllegalArgumentException,
      IOException {
    dictLookup = new DictionaryLookup(Dictionary.read(dictURL));
  }

  Resources tagRetriever = new Resources();
  
  
  private HashMap<List<String>, String> getLemmaTagsDict(String word) {
    List<WordData> wdList = dictLookup.lookup(word);
    HashMap<List<String>, String> dictMap = new HashMap<List<String>, String>();
    for (WordData wd : wdList) {
      List<String> wordLemmaTags = new ArrayList<String>();
      wordLemmaTags.add(word);
      wordLemmaTags.add(wd.getTag().toString());
      dictMap.put(wordLemmaTags, wd.getStem().toString());
    }
    return dictMap;
  }

  private List<String> getDictKeys(String lang, String word, String postag) {
    List<String> keys = new ArrayList<String>();
    String constantTag = tagRetriever.setTagConstant(lang, postag);
    if (postag.startsWith(String.valueOf(constantTag))) {
      keys.addAll(Arrays.asList(word, postag));
    } else {
      keys.addAll(Arrays.asList(word.toLowerCase(), postag));
    }
    return keys;
  }

  private HashMap<List<String>, String> getDictMap(String lang, String word, String postag) {
    HashMap<List<String>, String> dictMap = new HashMap<List<String>, String>();
    String constantTag = tagRetriever.setTagConstant(lang, postag);
    if (postag.startsWith(String.valueOf(constantTag))) {
      dictMap = this.getLemmaTagsDict(word);
    } else {
      dictMap = this.getLemmaTagsDict(word.toLowerCase());
    }
    return dictMap;
  }

  public String lemmatize(String lang, String word, String postag) {
    String lemma = null;
    List<String> keys = this.getDictKeys(lang, word, postag);
    HashMap<List<String>, String> dictMap = this.getDictMap(lang, word, postag);
    // lookup lemma as value of the map
    String keyValue = dictMap.get(keys);
    String constantTag = tagRetriever.setTagConstant(lang, postag);
    if (keyValue != null) {
      lemma = keyValue;
    } else if (keyValue == null && postag.startsWith(String.valueOf(constantTag))) {
      lemma = word;
    } else if (keyValue == null && word.toUpperCase() == word) {
      lemma = word;
    } else {
      lemma = word.toLowerCase();
    }
    return lemma;
  }

}
