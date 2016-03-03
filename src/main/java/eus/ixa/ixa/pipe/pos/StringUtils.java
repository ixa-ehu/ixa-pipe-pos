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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import opennlp.tools.util.Span;

import com.google.common.io.Files;

/**
 * Pattern matching and other utility string functions.
 * 
 * @author ragerri
 * @version 2013-03-19
 */
public final class StringUtils {

  /**
   * Private constructor.
   */
  private StringUtils() {
    throw new AssertionError("This class is not meant to be instantiated!");
  }

  /**
   * Finds a pattern (typically a named entity string) in a tokenized sentence.
   * It outputs the {@link Span} indexes of the named entity found, if any.
   * 
   * @param pattern
   *          a string to find
   * @param tokens
   *          an array of tokens
   * @return token spans of the pattern (e.g. a named entity)
   */
  public static List<Integer> exactTokenFinderIgnoreCase(final String pattern,
      final String[] tokens) {
    final String[] patternTokens = pattern.split(" ");
    int i, j;
    final int patternLength = patternTokens.length;
    final int sentenceLength = tokens.length;
    final List<Integer> neTokens = new ArrayList<Integer>();
    for (j = 0; j <= sentenceLength - patternLength; ++j) {
      for (i = 0; i < patternLength
          && patternTokens[i].equalsIgnoreCase(tokens[i + j]); ++i) {
        ;
      }
      if (i >= patternLength) {
        neTokens.add(j);
        neTokens.add(i + j);
      }
    }
    return neTokens;
  }

  /**
   * Finds a pattern (typically a named entity string) in a tokenized sentence.
   * It outputs the {@link Span} indexes of the named entity found, if any
   * 
   * @param pattern
   *          a string to find
   * @param tokens
   *          an array of tokens
   * @return token spans of the pattern (e.g. a named entity)
   */
  public static List<Integer> exactTokenFinder(final String pattern,
      final String[] tokens) {
    final String[] patternTokens = pattern.split(" ");
    int i, j;
    final int patternLength = patternTokens.length;
    final int sentenceLength = tokens.length;
    final List<Integer> neTokens = new ArrayList<Integer>();
    for (j = 0; j <= sentenceLength - patternLength; ++j) {
      for (i = 0; i < patternLength && patternTokens[i].equals(tokens[i + j]); ++i) {
        ;
      }
      if (i >= patternLength) {
        neTokens.add(j);
        neTokens.add(i + j);
      }
    }
    return neTokens;
  }

  /**
   * Finds a pattern (typically a named entity string) in a sentence string. It
   * outputs the offsets for the start and end characters named entity found, if
   * any.
   * 
   * @param pattern
   *          the pattern to be searched
   * @param sentence
   *          the sentence
   * @return a list of integers corresponding to the characters of the string
   *         found
   */
  public static List<Integer> exactStringFinder(final String pattern,
      final String sentence) {
    final char[] patternArray = pattern.toCharArray(), sentenceArray = sentence
        .toCharArray();
    int i, j;
    final int patternLength = patternArray.length;
    final int sentenceLength = sentenceArray.length;
    final List<Integer> neChars = new ArrayList<Integer>();
    for (j = 0; j <= sentenceLength - patternLength; ++j) {
      for (i = 0; i < patternLength && patternArray[i] == sentenceArray[i + j]; ++i) {
        ;
      }
      if (i >= patternLength) {
        neChars.add(j);
        neChars.add(i + j);
      }
    }
    return neChars;
  }

  /**
   * 
   * It takes a NE span indexes and the tokens in a sentence and produces the
   * string to which the NE span corresponds to. This function is used to get
   * the Named Entity or Name textual representation from a {@link Span}
   * 
   * @param reducedSpan
   *          a {@link Span}
   * @param tokens
   *          an array of tokens
   * @return named entity string
   */
  public static String getStringFromSpan(final Span reducedSpan,
      final String[] tokens) {
    final StringBuilder sb = new StringBuilder();
    for (int si = reducedSpan.getStart(); si < reducedSpan.getEnd(); si++) {
      sb.append(tokens[si]).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Gets the String joined by a space of an array of tokens.
   * 
   * @param tokens
   *          an array of tokens representing a tokenized sentence
   * @return sentence the sentence corresponding to the tokens
   */
  public static String getStringFromTokens(final String[] tokens) {
    final StringBuilder sb = new StringBuilder();
    for (final String tok : tokens) {
      sb.append(tok).append(" ");
    }
    return sb.toString().trim();
  }
  
  public static String getSetStringFromList(List<String> posLemmaValues) {
    final StringBuilder sb = new StringBuilder();
    HashSet<String> posLemmaSet = new LinkedHashSet<String>(posLemmaValues);
    for (final String tok : posLemmaSet) {
      sb.append(tok).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Recursively get every file in a directory and add them to a list.
   * 
   * @param inputPath
   *          the input directory
   * @return the list containing all the files
   */
  public static List<File> getFilesInDir(final File inputPath) {
    final List<File> fileList = new ArrayList<File>();
    for (final File aFile : Files.fileTreeTraverser().preOrderTraversal(
        inputPath)) {
      if (aFile.isFile()) {
        fileList.add(aFile);
      }
    }
    return fileList;
  }
  
  
  /**
   * Get mininum of three values.
   * @param a number a
   * @param b number b
   * @param c number c
   * @return the minimum
   */
  private static int minimum(int a, int b, int c) {
      int minValue;
      minValue = a;
      if (b < minValue) {
        minValue = b;
      }
      if (c < minValue) {
        minValue = c;
      }
      return minValue;
  }
  
  /**
   * Computes the Levenshtein distance of two strings in a matrix.
   * Based on pseudo-code provided here:
   * https://en.wikipedia.org/wiki/Levenshtein_distance#Computing_Levenshtein_distance
   * which in turn is based on the paper Wagner, Robert A.; Fischer, Michael J. (1974),
   * "The String-to-String Correction Problem", Journal of the ACM 21 (1): 168-173
   * @param wordForm the form
   * @param lemma the lemma
   * @return the distance
   */
  public static int[][] levenshteinDistance(String wordForm, String lemma) {

    int wordLength = wordForm.length();
    int lemmaLength = lemma.length();
    int cost;
    int[][] distance = new int[wordLength + 1][lemmaLength + 1];
    
    if (wordLength == 0) {
      return distance;
    }
    if (lemmaLength == 0) {
      return distance;
    }
    //fill in the rows of column 0
    for (int i = 0; i <= wordLength; i++) {
      distance[i][0] = i;
    }
    //fill in the columns of row 0
    for (int j = 0; j <= lemmaLength; j++) {
      distance[0][j] = j;
    }
    //fill in the rest of the matrix calculating the minimum distance
    for (int i = 1; i <= wordLength; i++) {
      int s_i = wordForm.charAt(i - 1);
      for (int j = 1; j <= lemmaLength; j++) {
        if (s_i == lemma.charAt(j - 1)) {
          cost = 0;
        } else {
          cost = 1;
        }
        //obtain minimum distance from calculating deletion, insertion, substitution
        distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1, distance[i - 1][j - 1] + cost);
      }
    }
    return distance;
  }
  
  /**
   * Computes the Shortest Edit Script (SES) to convert a word into its lemma.
   * This is based on Chrupala's PhD thesis (2007).
 * @param wordForm the token
 * @param lemma the target lemma
 * @param distance the levenshtein distance
 * @param permutations the number of permutations
 */
public static void computeShortestEditScript(String wordForm, String lemma, int[][] distance, StringBuffer permutations) {
    
    int n = distance.length;
    int m = distance[0].length;
    
    int wordFormLength = n - 1;
    int lemmaLength = m - 1;
    while(true) {
        
        if (distance[wordFormLength][lemmaLength] == 0) {
          break;
        }
        if ((lemmaLength > 0 && wordFormLength > 0) && (distance[wordFormLength - 1][lemmaLength - 1] < distance[wordFormLength][lemmaLength])) {
            permutations.append('R').append(Integer.toString(wordFormLength - 1)).append(wordForm.charAt(wordFormLength - 1)).append(lemma.charAt(lemmaLength - 1));
            lemmaLength--;
            wordFormLength--;
            continue;
        }
        if (lemmaLength > 0 && (distance[wordFormLength][lemmaLength - 1] < distance[wordFormLength][lemmaLength])) {
            permutations.append('I').append(Integer.toString(wordFormLength)).append(lemma.charAt(lemmaLength - 1));
            lemmaLength--;
            continue;
        }
        if (wordFormLength > 0 && (distance[wordFormLength - 1][lemmaLength] < distance[wordFormLength][lemmaLength])) {
            permutations.append('D').append(Integer.toString(wordFormLength - 1)).append(wordForm.charAt(wordFormLength - 1));
            wordFormLength--;
            continue;
        }
        if ((wordFormLength > 0 && lemmaLength > 0) && (distance[wordFormLength - 1][lemmaLength - 1] == distance[wordFormLength][lemmaLength])) {
            wordFormLength--; lemmaLength--;
            continue ;
        }
        if (wordFormLength > 0 && (distance[wordFormLength - 1][lemmaLength] == distance[wordFormLength][lemmaLength])) {
            wordFormLength--;
            continue;
        }
        if (lemmaLength > 0 && (distance[wordFormLength][lemmaLength - 1] == distance[wordFormLength][lemmaLength])) {
            lemmaLength--;
            continue;
        }   
    }
}
  /**
   * Read predicted SES by the lemmatizer model and apply the
   * permutations to obtain the lemma from the wordForm.
   * @param wordForm the wordForm
   * @param permutations the permutations predicted by the lemmatizer model
   * @return the lemma
   */
  public static String decodeShortestEditScript(String wordForm, String permutations) {
    
    StringBuffer lemma = new StringBuffer(wordForm).reverse();
    
    int permIndex = 0;
    while(true) {
        if (permutations.length() <= permIndex) {
          break;
        }
        //read first letter of permutation string
        char nextOperation = permutations.charAt(permIndex);
        //System.err.println("-> NextOP: " + nextOperation);
        //go to the next permutation letter
        permIndex++;
        if (nextOperation == 'R') {
            String charAtPerm = Character.toString(permutations.charAt(permIndex));
            int charIndex = Integer.parseInt(charAtPerm);
            // go to the next character in the permutation buffer
            // which is the replacement character
            permIndex++;
            char replace = permutations.charAt(permIndex);
            //go to the next char in the permutation buffer
            // which is the candidate character
            permIndex++;
            char with = permutations.charAt(permIndex);
            
            if (lemma.length() <= charIndex) {
              return wordForm; 
            }
            if (lemma.charAt(charIndex) == replace) {
              lemma.setCharAt(charIndex, with);
            }
            //System.err.println("-> ROP: " + lemma.toString());
            //go to next permutation
            permIndex++;
            
        } else if (nextOperation == 'I') {
            String charAtPerm = Character.toString(permutations.charAt(permIndex));
            int charIndex = Integer.parseInt(charAtPerm);
            permIndex++;
            //character to be inserted
            char in = permutations.charAt(permIndex);
        
            if (lemma.length() < charIndex) {
              return wordForm; 
            }
            lemma.insert(charIndex, in);
            //System.err.println("-> IOP " + lemma.toString());
            //go to next permutation
            permIndex++;
        } else if (nextOperation == 'D') {
            String charAtPerm = Character.toString(permutations.charAt(permIndex));
            int charIndex = Integer.parseInt(charAtPerm);
            if (lemma.length() <= charIndex) {
              return wordForm;
            }
            lemma.deleteCharAt(charIndex);
            permIndex++;
            // go to next permutation
            permIndex++;
        }
    }
    return lemma.reverse().toString();
}
  
  /**
   * Get the SES required to go from a word to a lemma.
   * @param wordForm the word
   * @param lemma the lemma
   * @return the shortest edit script
   */
  public static String getShortestEditScript(String wordForm, String lemma) {
    String reversedWF = new StringBuffer(wordForm.toLowerCase()).reverse().toString();
    String reversedLemma = new StringBuffer(lemma.toLowerCase()).reverse().toString();
    StringBuffer permutations = new StringBuffer();
    String ses;
    if (!reversedWF.equals(reversedLemma)) {
      int[][]levenDistance = StringUtils.levenshteinDistance(reversedWF, reversedLemma);
      StringUtils.computeShortestEditScript(reversedWF, reversedLemma, levenDistance, permutations);
      ses = permutations.toString();
    } else {
      ses = "O";
    }
    return ses;
  }


}
