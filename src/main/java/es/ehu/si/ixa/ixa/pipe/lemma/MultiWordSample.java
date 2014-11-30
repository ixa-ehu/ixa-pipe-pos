package es.ehu.si.ixa.ixa.pipe.lemma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.util.Span;

/**
 * Class for holding multiwords for a single unit of text.
 */
public class MultiWordSample {

  private final String id;
  private final List<String> tokens;
  private final List<Span> names;
  public static final String DEFAULT_TYPE = "UNK";
  public static final Pattern spanPattern = Pattern.compile("<START:\\w+>\\s+(.*?)\\s+<END>");

  public MultiWordSample(String id, String[] sentence, Span[] multiwords) {

    this.id = id;
    if (sentence == null) {
      throw new IllegalArgumentException("sentence must not be null!");
    }
    if (multiwords == null) {
      multiwords = new Span[0];
    }
    this.tokens = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(sentence)));
    this.names = Collections.unmodifiableList(new ArrayList<Span>(Arrays.asList(multiwords)));
    // TODO: Check that multiword spans are not overlapping, otherwise throw exception
  }

  public MultiWordSample(String[] sentence, Span[] names) {
    this(null, sentence, names);
  }

  public String getId() {
    return id;
  }

  public String[] getSentence() {
    return tokens.toArray(new String[tokens.size()]);
  }

  public Span[] getNames() {
    return names.toArray(new Span[names.size()]);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    } else if (obj instanceof MultiWordSample) {
      MultiWordSample a = (MultiWordSample) obj;
      return Arrays.equals(getSentence(), a.getSentence()) &&
          Arrays.equals(getNames(), a.getNames());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {

      for (Span name : names) {
        if (name.getStart() == tokenIndex) {
          sb.append("<START:").append(name.getType()).append("> ");
        }

        if (name.getEnd() == tokenIndex) {
          sb.append("<END>").append(' ');
        }
      }
      sb.append(tokens.get(tokenIndex)).append(' ');
    }

    if (tokens.size() > 1)
      sb.setLength(sb.length() - 1);

    for (Span name : names) {
      if (name.getEnd() == tokens.size()) {
        sb.append(' ').append("<END>");
      }
    }
    String multiWordSample = sb.toString();
    Matcher spanMatcher = spanPattern.matcher(multiWordSample);
    while (spanMatcher.find()) {
      multiWordSample = spanMatcher.replaceAll(spanMatcher.group(1).replace(" ", "#"));
    }
    return multiWordSample;
  }
}

