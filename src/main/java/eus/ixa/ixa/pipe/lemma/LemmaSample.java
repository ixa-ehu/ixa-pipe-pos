/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eus.ixa.ixa.pipe.lemma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents an lemmatized sentence.
 */
public class LemmaSample {

  private List<String> tokens;

  private List<String> tags;
  
  private final List<String> preds;

  public LemmaSample(String[] tokens, String[] tags, String[] preds) {

    validateArguments(tokens.length, tags.length, preds.length);

    this.tokens = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(tokens)));
    this.tags = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(tags)));
    this.preds = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(preds)));
  }
  
  public LemmaSample(List<String> tokens, List<String> tags, List<String> preds) {

    validateArguments(tokens.size(), tags.size(), preds.size());

    this.tokens = Collections.unmodifiableList(new ArrayList<String>((tokens)));
    this.tags = Collections.unmodifiableList(new ArrayList<String>((tags)));
    this.preds = Collections.unmodifiableList(new ArrayList<String>((preds)));
  }

  /** Gets the tokens */
  public String[] getTokens() {
    return tokens.toArray(new String[tokens.size()]);
  }

  /** Gets the POS Tags for the sentence */
  public String[] getTags() {
    return tags.toArray(new String[tags.size()]);
  }

  /** Gets the Lemmas for the sentence */
  public String[] getPreds() {
    return preds.toArray(new String[preds.size()]);
  }

  private void validateArguments(int tokensSize, int tagsSize, int predsSize) throws IllegalArgumentException {
    if (tokensSize != tagsSize || tagsSize != predsSize) {
      throw new IllegalArgumentException(
          "All arrays must have the same length: " +
              "sentenceSize: " + tokensSize +
              ", tagsSize: " + tagsSize +
              ", predsSize: " + predsSize + "!");
    }
    if (tokens.contains(null)) {
      throw new IllegalArgumentException("null elements are not allowed in sentence tokens!");
    }
    if (tags.contains(null)) {
      throw new IllegalArgumentException("null elements are not allowed in tags!");
    }   
  }

  @Override
  public String toString() {

        StringBuilder lemmaString = new StringBuilder();

        for (int ci = 0; ci < preds.size(); ci++) {
        lemmaString.append(tokens.get(ci)).append(" ").append(tags.get(ci)).append(" ").append(preds.get(ci)).append("\n");
        }
        return lemmaString.toString();
      }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof LemmaSample) {
      LemmaSample a = (LemmaSample) obj;
      return Arrays.equals(getTokens(), a.getTokens())
          && Arrays.equals(getTags(), a.getTags())
          && Arrays.equals(getPreds(), a.getPreds());
    } else {
      return false;
    }
  }
}
