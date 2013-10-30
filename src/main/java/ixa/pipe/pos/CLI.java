/*
 * Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.pos;

import ixa.kaflib.KAFDocument;
import ixa.pipe.lemmatize.Dictionary;
import ixa.pipe.lemmatize.JWNLemmatizer;
import ixa.pipe.lemmatize.MorfologikLemmatizer;
import ixa.pipe.lemmatize.SimpleLemmatizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import net.didion.jwnl.JWNLException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import org.jdom2.JDOMException;

/**
 * IXA-OpenNLP POS tagging using Apache OpenNLP. Lemmatization is dictionary based.
 *
 * @author ragerri
 * @version 1.0
 *
 */

public class CLI {

  /**
   *
   *
   * BufferedReader (from standard input) and BufferedWriter are opened. The
   * module takes KAF and reads the header, and the text elements and uses
   * Annotate class to annotate POS tags and obtain lemmas. The terms elements
   * with POS annotation are then added to the KAF received via standard input.
   * Finally, the modified KAF document is passed via standard output.
   *
   * @param args
   * @throws IOException
   * @throws JDOMException
   * @throws JWNLException
   */

  public static void main(String[] args) throws IOException, JDOMException,
      JWNLException {

    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-pipe-pos-1.0.jar")
        .description(
            "ixa-opennlp-pos-1.0 is a multilingual POS tagger module developed by IXA NLP Group based on Apache OpenNLP.\n");
    MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup("group");
    // specify language
    parser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(false)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-pos");

    // specify lemmatization method

    group.addArgument("-lem","--lemmatize").choices("bin","plain").setDefault("bin").help("Lemmatization method." +
    		"Choose 'bin' for fast binary Morfologik dictionary (this is the default) " +
    		"; 'plain' for plain text dictionary. ");

    group
        .addArgument("-w", "--wordnet")
        .required(false)
        .help(
            "Provide path to a dictionary to perform lemmatization with WordNet 3.0. Choices are between WordNet 3.0 and " +
            "an in-house created dictionary");


    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-pipe-pos-1.0.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language and dictionary parameters and construct annotators, read
     * and write kaf
     */

    // String wnDirectory = args[0];

    String lemMethod = parsedArguments.getString("lemmatize");
    String dictionary = parsedArguments.getString("wordnet");
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    try {
      breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

      // read KAF from standard input
      KAFDocument kaf = KAFDocument.createFromStream(breader);

      // language parameter
      String lang;
      if (parsedArguments.get("lang") == null) {
	  lang = kaf.getLang();
      }
      else {
	  lang =  parsedArguments.getString("lang");
      }
      // choosing Dictionary for lemmatization
      // WordNet lemmatization available for English only
      // Default: MorfologikLemmatizer

      Dictionary lemmatizer = null;
      Resources resourceRetriever = new Resources();

      if (lemMethod.equalsIgnoreCase("plain")) {
    	  System.err.println("Using plain text dictionary");
	    	InputStream dictLemmatizer = resourceRetriever.getDictionary(lang);
	    	lemmatizer = new SimpleLemmatizer(dictLemmatizer);
	  }

      else if (dictionary != null && lang.equalsIgnoreCase("en")) {
    	  lemmatizer = new JWNLemmatizer(dictionary);
      }

      else if (dictionary != null && lang.equalsIgnoreCase("en") == false) {
    	  System.err.println("WordNet lemmatization available for English only. Using" +
    	  		" default Morfologik binary dictionary.");
    	  URL dictLemmatizer = resourceRetriever.getBinaryDict(lang);
	      lemmatizer = new MorfologikLemmatizer(dictLemmatizer);
      }

      else {
		  System.err.println("Using default Morfologik binary dictionary.");
    	  URL dictLemmatizer = resourceRetriever.getBinaryDict(lang);
	      lemmatizer = new MorfologikLemmatizer(dictLemmatizer);
	  }

      // add already contained header plus this module linguistic
      // processor
      kaf.addLinguisticProcessor("terms","ixa-pipe-pos-"+lang,"1.0");
      Annotate annotator = new Annotate(lang);

      // annotate POS tags to KAF and lemmatize
      annotator.annotatePOSToKAF(kaf, lemmatizer, lang);
      bwriter.write(kaf.toString());
      bwriter.close();
      breader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
