package es.ehu.si.ixa.pipe.pos.train;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSContextGenerator;
import opennlp.tools.util.Cache;
import opennlp.tools.util.StringList;

/**
 * A context generator for the POS Tagger.
 */
public class BaselineContextGenerator implements POSContextGenerator {

  protected final String SE = "*SE*";
  protected final String SB = "*SB*";
  private static final int PREFIX_LENGTH = 4;
  private static final int SUFFIX_LENGTH = 4;

  private static Pattern hasCap = Pattern.compile("[A-Z]");
  private static Pattern hasNum = Pattern.compile("[0-9]");

  private Cache contextsCache;
  private Object wordsKey;

  private Dictionary dict;
  private String[] dictGram;

  /**
   * Initializes the current instance.
   *
   * @param dict
   */
  public BaselineContextGenerator(Dictionary dict) {
    this(0,dict);
  }

  /**
   * Initializes the current instance.
   *
   * @param cacheSize
   * @param dict
   */
  public BaselineContextGenerator(int cacheSize, Dictionary dict) {
    this.dict = dict;
    dictGram = new String[1];
    if (cacheSize > 0) {
      contextsCache = new Cache(cacheSize);
    }
  }
  protected static String[] getPrefixes(String lex) {
    String[] prefs = new String[PREFIX_LENGTH];
    for (int li = 2, ll = PREFIX_LENGTH; li < ll; li++) {
      prefs[li] = lex.substring(0, Math.min(li + 1, lex.length()));
    }
    return prefs;
  }

  protected static String[] getSuffixes(String lex) {
    String[] suffs = new String[SUFFIX_LENGTH];
    for (int li = 0, ll = SUFFIX_LENGTH; li < ll; li++) {
      suffs[li] = lex.substring(Math.max(lex.length() - li - 1, 0));
    }
    return suffs;
  }

  public String[] getContext(int index, String[] sequence, String[] priorDecisions, Object[] additionalContext) {
    return getContext(index,sequence,priorDecisions);
  }

  /**
   * Returns the context for making a pos tag decision at the specified token index given the specified tokens and previous tags.
   * @param index The index of the token for which the context is provided.
   * @param tokens The tokens in the sentence.
   * @param tags The tags assigned to the previous words in the sentence.
   * @return The context for making a pos tag decision at the specified token index given the specified tokens and previous tags.
   */
  public String[] getContext(int index, Object[] tokens, String[] tags) {
    String next, nextnext, lex, prev, prevprev;
    String tagprev, tagprevprev, tagnext, tagnextnext;
    tagnext = tagnextnext = tagprev = tagprevprev = null;
    next = nextnext = lex = prev = prevprev = null;

    lex = tokens[index].toString();
    if (tokens.length > index + 1) {
      next = tokens[index + 1].toString();
      if (tokens.length > index + 2)
        nextnext = tokens[index + 2].toString();
      else
        nextnext = SE; // Sentence End

    }
    else {
      next = SE; // Sentence End
    }
    
    if (tags.length > index + 1) {
      tagnext = tags[index + 1];
      if (tags.length > index + 2) {
        tagnextnext = tags[index +2];
      }
    }

    if (index - 1 >= 0) {
      prev =  tokens[index - 1].toString();
      tagprev =  tags[index - 1];

      if (index - 2 >= 0) {
        prevprev = tokens[index - 2].toString();
        tagprevprev = tags[index - 2];
      }
      else {
        prevprev = SB; // Sentence Beginning
      }
    }
    else {
      prev = SB; // Sentence Beginning
    }
    String cacheKey = index+tagprev+tagprevprev;
    if (contextsCache != null) {
      if (wordsKey == tokens){
        String[] cachedContexts = (String[]) contextsCache.get(cacheKey);
        if (cachedContexts != null) {
          return cachedContexts;
        }
      }
      else {
        contextsCache.clear();
        wordsKey = tokens;
      }
    }
    List<String> featureList = new ArrayList<String>();
    featureList.add("default");
    // add the word itself
    featureList.add("w=" + lex);
    dictGram[0] = lex;
    if (dict == null || !dict.contains(new StringList(dictGram))) {
      // do some basic suffix analysis
      String[] suffs = getSuffixes(lex);
      for (int i = 0; i < suffs.length; i++) {
        featureList.add("suf=" + suffs[i]);
      }

      String[] prefs = getPrefixes(lex);
      for (int i = 0; i < prefs.length; i++) {
        featureList.add("pre=" + prefs[i]);
      }
      // see if the word has any special characters
      if (lex.indexOf('-') != -1) {
        featureList.add("h");
      }

      if (hasCap.matcher(lex).find()) {
        featureList.add("c");
      }

      if (hasNum.matcher(lex).find()) {
        featureList.add("d");
      }
    }
    // add the words and pos's of the surrounding context
    if (prev != null) {
      featureList.add("pw=" + prev);
      //bigram w-1,w
      featureList.add("pw,w=" + prev + "," + lex);
      if (tagprev != null) {
        featureList.add("pt=" + tagprev);
        //bigram tag-1, w
        featureList.add("pt,w=" + tagprev + "," + lex);
        if (tagnext != null) {
          featureList.add("pt,nt=" + tagprev + "," + tagnext);
          featureList.add("pt,nt,w=" + tagprev + "," + tagnext + "," + lex);
        }
      }
      if (prevprev != null) {
        featureList.add("ppw=" + prevprev);
        if (tagprevprev != null) {
          featureList.add("pt2=" + tagprevprev);
          featureList.add("pt2,w=" + tagprevprev + "," + lex);
          //bigram tag-2,tag-1
          featureList.add("pt2,pt1=" + tagprevprev+","+tagprev);
          //trigram tag-2,tag-1,w
          featureList.add("pt2,pt1,w=" + tagprevprev + "," + tagprev + "," + lex);
        }
      }
    }

    if (next != null) {
      featureList.add("nw=" + next);
      //bigram w,w+1
      featureList.add("nw,w=" + lex + "," + next);
      if (tagnext != null) {
        featureList.add("nt=" + tagnext);
        featureList.add("nt,w=" + tagnext + "," + lex);
      }
      if (nextnext != null) {
        featureList.add("nnw=" + nextnext);
        if (tagnextnext != null) {
          featureList.add("nnt=" + tagnextnext);
          featureList.add("nt,nnt=" + tagnext + "," + tagnextnext);
          featureList.add("nt,nnt,w=" + tagnext + "," + tagnextnext + "," + lex);
          featureList.add("nnt,w=" + tagnextnext + "," + lex);
        }
  
      }
    }
    String[] contexts = featureList.toArray(new String[featureList.size()]);
    if (contextsCache != null) {
      contextsCache.put(cacheKey,contexts);
    }
    return contexts;
  }

}

