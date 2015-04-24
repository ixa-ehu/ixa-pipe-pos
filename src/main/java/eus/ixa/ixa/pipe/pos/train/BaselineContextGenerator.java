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
package eus.ixa.ixa.pipe.pos.train;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSContextGenerator;
import opennlp.tools.util.Cache;
import opennlp.tools.util.StringList;

/**
 * An improved context generator for the POS Tagger. This baseline generator
 * provides more contextual features such as bigrams to the
 * {@code @DefaultPOSContextGenerator}. These extra features require at least
 * 2GB memory to train, more if training data is large.
 * 
 * @author ragerri
 * @version 2014-07-08
 */
public class BaselineContextGenerator implements POSContextGenerator {

  /**
   * The ending string.
   */
  private final String SE = "*SE*";
  /**
   * The starting string.
   */
  private final String SB = "*SB*";
  /**
   * Default prefix length.
   */
  private static final int PREFIX_LENGTH = 4;
  /**
   * Default suffix length.
   */
  private static final int SUFFIX_LENGTH = 4;
  /**
   * Has capital regexp.
   */
  private static Pattern hasCap = Pattern.compile("[A-Z]");
  /**
   * Has number regexp.
   */
  private static Pattern hasNum = Pattern.compile("[0-9]");
  /**
   * The context Cache.
   */
  private Cache contextsCache;
  /**
   * The words key.
   */
  private Object wordsKey;
  /**
   * The tag dictionary.
   */
  private final Dictionary dict;
  /**
   * The dictionary ngrams.
   */
  private final String[] dictGram;

  /**
   * Initializes the current instance.
   * 
   * @param aDict
   *          the dictionary
   */
  public BaselineContextGenerator(final Dictionary aDict) {
    this(0, aDict);
  }

  /**
   * Initializes the current instance.
   * 
   * @param cacheSize
   *          the cache size
   * @param aDict
   *          the dictionary
   */
  public BaselineContextGenerator(final int cacheSize, final Dictionary aDict) {
    this.dict = aDict;
    this.dictGram = new String[1];
    if (cacheSize > 0) {
      this.contextsCache = new Cache(cacheSize);
    }
  }

  /**
   * Obtain prefixes for each token.
   * 
   * @param lex
   *          the current word
   * @return the prefixes
   */
  protected static String[] getPrefixes(final String lex) {
    final String[] prefs = new String[PREFIX_LENGTH];
    for (int li = 0, ll = PREFIX_LENGTH; li < ll; li++) {
      prefs[li] = lex.substring(0, Math.min(li + 1, lex.length()));
    }
    return prefs;
  }

  /**
   * Obtain suffixes for each token.
   * 
   * @param lex
   *          the word
   * @return the suffixes
   */
  protected static String[] getSuffixes(final String lex) {
    final String[] suffs = new String[SUFFIX_LENGTH];
    for (int li = 0, ll = SUFFIX_LENGTH; li < ll; li++) {
      suffs[li] = lex.substring(Math.max(lex.length() - li - 1, 0));
    }
    return suffs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see opennlp.tools.postag.POSContextGenerator#getContext(int,
   * java.lang.String[], java.lang.String[], java.lang.Object[])
   */
  public final String[] getContext(final int index, final String[] sequence,
      final String[] priorDecisions, final Object[] additionalContext) {
    return getContext(index, sequence, priorDecisions);
  }

  /**
   * Returns the context for making a pos tag decision at the specified token
   * index given the specified tokens and previous tags.
   * 
   * @param index
   *          The index of the token for which the context is provided.
   * @param tokens
   *          The tokens in the sentence.
   * @param tags
   *          The tags assigned to the previous words in the sentence.
   * @return The context for making a pos tag decision at the specified token
   *         index given the specified tokens and previous tags.
   */
  public final String[] getContext(final int index, final Object[] tokens,
      final String[] tags) {
    String next, nextnext, lex, prev, prevprev;
    String tagprev, tagprevprev;
    tagprev = tagprevprev = null;
    next = nextnext = lex = prev = prevprev = null;

    lex = tokens[index].toString();
    if (tokens.length > index + 1) {
      next = tokens[index + 1].toString();
      if (tokens.length > index + 2) {
        nextnext = tokens[index + 2].toString();
      } else {
        nextnext = this.SE; // Sentence End
      }

    } else {
      next = this.SE; // Sentence End
    }

    if (index - 1 >= 0) {
      prev = tokens[index - 1].toString();
      tagprev = tags[index - 1];

      if (index - 2 >= 0) {
        prevprev = tokens[index - 2].toString();
        tagprevprev = tags[index - 2];
      } else {
        prevprev = this.SB; // Sentence Beginning
      }
    } else {
      prev = this.SB; // Sentence Beginning
    }
    final String cacheKey = index + tagprev + tagprevprev;
    if (this.contextsCache != null) {
      if (this.wordsKey == tokens) {
        final String[] cachedContexts = (String[]) this.contextsCache
            .get(cacheKey);
        if (cachedContexts != null) {
          return cachedContexts;
        }
      } else {
        this.contextsCache.clear();
        this.wordsKey = tokens;
      }
    }
    final List<String> featureList = new ArrayList<String>();
    featureList.add("default");
    // add the word itself
    featureList.add("w=" + lex);
    this.dictGram[0] = lex;
    if (this.dict == null || !this.dict.contains(new StringList(this.dictGram))) {
      // do some basic suffix analysis
      final String[] suffs = getSuffixes(lex);
      for (final String suff : suffs) {
        featureList.add("suf=" + suff);
      }

      final String[] prefs = getPrefixes(lex);
      for (final String pref : prefs) {
        featureList.add("pre=" + pref);
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
      // bigram w-1,w
      featureList.add("pw,w=" + prev + "," + lex);
      if (tagprev != null) {
        featureList.add("pt=" + tagprev);
        // bigram tag-1, w
        featureList.add("pt,w=" + tagprev + "," + lex);
      }
      if (prevprev != null) {
        featureList.add("ppw=" + prevprev);
        if (tagprevprev != null) {
          // bigram tag-2,tag-1
          featureList.add("pt2,pt1=" + tagprevprev + "," + tagprev);
        }
      }
    }

    if (next != null) {
      featureList.add("nw=" + next);
      if (nextnext != null) {
        featureList.add("nnw=" + nextnext);

      }
    }
    final String[] contexts = featureList
        .toArray(new String[featureList.size()]);
    if (this.contextsCache != null) {
      this.contextsCache.put(cacheKey, contexts);
    }
    return contexts;
  }

}
