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
 * Class for objects containing morphological information.
 * 
 * @author ragerri
 * @version 2014-07-08
 * 
 */
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

  /**
   * Construct a morpheme object.
   * 
   * @param aWord
   *          the word
   * @param aTag
   *          the tag
   */
  public Morpheme(final String aWord, final String aTag) {
    this.word = aWord;
    this.tag = aTag.toUpperCase();
  }

  /**
   * Construct a morpheme object with lemma.
   * 
   * @param aWord
   *          the word
   * @param aTag
   *          the tag
   * @param aLemma
   *          the lemma
   */
  public Morpheme(final String aWord, final String aTag, final String aLemma) {
    this.word = aWord;
    this.tag = aTag.toUpperCase();
    this.lemma = aLemma;
  }

  /**
   * Get the word.
   * 
   * @return the word
   */
  public final String getWord() {
    return this.word;
  }

  /**
   * Get the morphological tag.
   * 
   * @return the morphological tag
   */
  public final String getTag() {
    return this.tag;
  }

  /**
   * Get the lemma.
   * 
   * @return the lemma
   */
  public final String getLemma() {
    return this.lemma;
  }

  /**
   * Set the value of the word.
   * 
   * @param aWord
   *          the word
   */
  public final void setValue(final String aWord) {
    this.word = aWord;
  }

  /**
   * Set the morphological tag.
   * 
   * @param aTag
   *          the morphological tag
   */
  public final void setTag(final String aTag) {
    this.tag = aTag.toUpperCase();
  }

  /**
   * Set the lemma.
   * 
   * @param aLemma
   *          the lemma
   */
  public final void setLemma(final String aLemma) {
    this.lemma = aLemma;
  }

}
