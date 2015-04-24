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
package eus.ixa.ixa.pipe.pos;

/**
 * Class for creating {@code Morpheme} objects.
 * 
 * @author ragerri
 * @version 2014-07-08
 * 
 */
public class MorphoFactory {

  /**
   * Construct morpheme object with word and morphological tag.
   * 
   * @param word
   *          the word
   * @param tag
   *          the morphological tag
   * @return the morpheme object
   */
  public final Morpheme createMorpheme(final String word, final String tag) {
    final Morpheme morpheme = new Morpheme();
    morpheme.setValue(word);
    morpheme.setTag(tag);
    return morpheme;
  }

  /**
   * Construct morpheme object with word, tag and lemma.
   * 
   * @param word
   *          the word
   * @param tag
   *          the tag
   * @param lemma
   *          the lemma
   * @return the morphological object
   */
  public final Morpheme createMorpheme(final String word, final String tag,
      final String lemma) {
    final Morpheme morpheme = new Morpheme();
    morpheme.setValue(word);
    morpheme.setTag(tag);
    morpheme.setLemma(lemma);
    return morpheme;
  }

}
