/* 
 *Copyright 2014 Rodrigo Agerri

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
package eus.ixa.ixa.pipe.pos.dict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import opennlp.tools.util.Span;

/**
 * Class for holding multiwords for a sentence.
 * 
 * @author ragerri
 * @version 2014-11-30
 */
public class MultiWordSample {

  private final String id;
  private final List<String> tokens;
  private final List<Span> names;
  public static final String DEFAULT_TYPE = "UNK";

  public MultiWordSample(final String id, final String[] sentence,
      Span[] multiwords) {

    this.id = id;
    if (sentence == null) {
      throw new IllegalArgumentException("sentence must not be null!");
    }
    if (multiwords == null) {
      multiwords = new Span[0];
    }
    this.tokens = Collections.unmodifiableList(new ArrayList<String>(Arrays
        .asList(sentence)));
    this.names = Collections.unmodifiableList(new ArrayList<Span>(Arrays
        .asList(multiwords)));
    // TODO: Check that multiword spans are not overlapping, otherwise throw
    // exception
  }

  public MultiWordSample(final String[] sentence, final Span[] names) {
    this(null, sentence, names);
  }

  public String getId() {
    return this.id;
  }

  public String[] getSentence() {
    return this.tokens.toArray(new String[this.tokens.size()]);
  }

  public Span[] getNames() {
    return this.names.toArray(new Span[this.names.size()]);
  }

  @Override
  public int hashCode() {
    assert false : "hashCode not designed";
    return 42; // any arbitrary constant will do
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    } else if (obj instanceof MultiWordSample) {
      final MultiWordSample a = (MultiWordSample) obj;
      return Arrays.equals(getSentence(), a.getSentence())
          && Arrays.equals(getNames(), a.getNames());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    for (int tokenIndex = 0; tokenIndex < this.tokens.size(); tokenIndex++) {

      for (final Span name : this.names) {
        if (name.getStart() == tokenIndex) {
          sb.append("<START:").append(name.getType()).append("> ");
        }

        if (name.getEnd() == tokenIndex) {
          sb.append("<END>").append(' ');
        }
      }
      sb.append(this.tokens.get(tokenIndex)).append(' ');
    }

    if (this.tokens.size() > 1) {
      sb.setLength(sb.length() - 1);
    }

    for (final Span name : this.names) {
      if (name.getEnd() == this.tokens.size()) {
        sb.append(' ').append("<END>");
      }
    }
    final String multiWordSample = sb.toString();
    return multiWordSample;
  }

}
