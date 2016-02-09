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

public class DefaultLemmatizerContextGenerator implements LemmatizerContextGenerator {

  public DefaultLemmatizerContextGenerator() {
  }

  public String[] getContext(int index, String[] sequence, String[] priorDecisions, Object[] additionalContext) {
    return getContext(index,sequence,(String[]) additionalContext[0], priorDecisions);
  }

  public String[] getContext(int i, String[] toks, String[] tags, String[] preds) {
    // Words in a 5-word window
    String w0;

    // Tags in a 5-word window
    String t0;

    // Previous predictions
    String p_1;

    if (i < 1) {
      p_1 = "p_1=bos";
    }
    else {
      p_1 = "p_1=" + preds[i - 1];
    }

    w0 = "w0=" + toks[i];
    t0 = "t0=" + tags[i];


    String[] features = new String[] {
        //add word features
        w0,

        //add tag features
        t0,

        //add pred tags
        p_1,

        //add pred and tag
        p_1 + t0,

        //add pred and word
        p_1 + w0,
    };

    return features;
  }
}
