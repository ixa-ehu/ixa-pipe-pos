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
package eus.ixa.ixa.pipe.pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.postag.POSSample;
import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.ObjectStream;

/**
 * A stream filter which reads a sentence per line which contains
 * words and tags in word_tag format and outputs a {@link POSSample} objects.
 */
public class MorphoSampleStream extends FilterObjectStream<String, POSSample> {

  public MorphoSampleStream(ObjectStream<String> samples) {
    super(samples);
  }

  /**
   * Parses the next sentence and return the next
   * {@link POSSample} object.
   *
   * If an error occurs an empty {@link POSSample} object is returned
   * and an warning message is logged. Usually it does not matter if one
   * of many sentences is ignored.
   *
   * TODO: An exception in error case should be thrown.
   */
  public POSSample read() throws IOException {

    List<String> toks = new ArrayList<String>();
    List<String> tags = new ArrayList<String>();

    for (String line = samples.read(); line != null && !line.equals(""); line = samples.read()) {
      String[] parts = line.split("\t");
      if (parts.length != 3) {
        System.err.println("Skipping corrupt line: " + line);
      }
      else {
        toks.add(parts[0]);
        tags.add(parts[1]);
      }
    }
    if (toks.size() > 0) {
      POSSample posSample = new POSSample(toks.toArray(new String[toks.size()]), tags.toArray(new String[tags.size()]));
      //System.err.println(posSample.toString());
      return posSample;
    }
    else {
      return null;
    }
  }
}
