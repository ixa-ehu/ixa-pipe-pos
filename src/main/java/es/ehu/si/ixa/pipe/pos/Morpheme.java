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

package es.ehu.si.ixa.pipe.pos;

public class Morpheme {

  /**
   * The surface form of the morpheme, e.g., the word.
   */
  private String word;
  /**
   * The morphological tag of the morpheme.
   */
  private String tag;
  /**
   * The lemma of the morpheme.
   */
  private String lemma;

  /**
   * Create a new <code>Morpheme</code> with a null content (i.e., word).
   */
  public Morpheme() {
  }

  public Morpheme(final String aWord, final String aTag) {
    this.word = aWord;
    this.tag = aTag.toUpperCase();
  }

  public Morpheme(final String aWord, final String aTag, final String aLemma) {
    this.word = aWord;
    this.tag = aTag.toUpperCase();
    this.lemma = aLemma;
  }

  public final String getWord() {
    return word;
  }

  public final String getTag() {
    return tag;
  }

  public final String getLemma() {
    return lemma;
  }

  public final void setValue(final String aWord) {
    word = aWord;
  }

  public final void setTag(final String aTag) {
    tag = aTag.toUpperCase();
  }

  public final void setLemma(final String aLemma) {
    lemma = aLemma;
  }

}
