/*
 * Copyright 2016 Rodrigo Agerri

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

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.jdom2.JDOMException;

import com.google.common.io.Files;

import eus.ixa.ixa.pipe.ml.utils.Flags;

/**
 * Main class of ixa-pipe-pos which uses the ixa-pipe-ml API for POS tagging
 * and lemmatization. The annotate method is the main entry point.
 * @author ragerri
 * @version 2016-04-22
 */
public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-pos by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage()
      .getImplementationVersion();
  /**
   * Get the git commit of the ixa-pipe-pos compiled by looking at the MANIFEST
   * file.
   */
  private final String commit = CLI.class.getPackage()
      .getSpecificationVersion();
  /**
   * The CLI arguments.
   */
  private Namespace parsedArguments = null;
  /**
   * The argument parser.
   */
  private final ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-pos-" + this.version + "-exec.jar").description(
      "ixa-pipe-pos-" + this.version
          + " is a multilingual POS tagger and Lemmatizer developed by IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private final Subparsers subParsers = this.argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private final Subparser annotateParser;
  /**
   * Parser to start TCP socket for server-client functionality.
   */
  private Subparser serverParser;
  /**
   * Sends queries to the serverParser for annotation.
   */
  private Subparser clientParser;

  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    this.annotateParser = this.subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
    serverParser = subParsers.addParser("server").help("Start TCP socket server");
    loadServerParameters();
    clientParser = subParsers.addParser("client").help("Send queries to the TCP socket server");
    loadClientParameters();
  }

  /**
   * The main method to process the CLI.
   * @param args the command line arguments
   * @throws JDOMException if xml mal-formed NAF
   * @throws IOException if io problems
   */
  public static void main(final String[] args) throws JDOMException,
      IOException {

    final CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command line options.
   * @param args
   *          the arguments
   * @throws IOException
   *           if io error
   * @throws JDOMException
   *           if malformed XML
   */
  public final void parseCLI(final String[] args) throws IOException,
      JDOMException {
    try {
      this.parsedArguments = this.argParser.parseArgs(args);
      System.err.println("CLI options: " + this.parsedArguments);
      if (args[0].equals("tag")) {
        annotate(System.in, System.out);
      } else if (args[0].equals("server")) {
        server();
      } else if (args[0].equals("client")) {
        client(System.in, System.out);
      }
    } catch (final ArgumentParserException e) {
      this.argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-pos-" + this.version
          + "-exec.jar (tag|server|client) -help for details");
      System.exit(1);
    }
  }

  /**
   * Main entry point for annotation. Takes system in as input and outputs
   * annotated text via system out.
   * 
   * @param inputStream
   *          the input stream
   * @param outputStream
   *          the output stream
   * @throws IOException
   *           the exception if not input is provided
   * @throws JDOMException
   *           if malformed XML
   */
  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException, JDOMException {

    final String model = this.parsedArguments.getString("model");
    final String lemmatizerModel = this.parsedArguments
        .getString("lemmatizerModel");
    final boolean allMorphology = this.parsedArguments
        .getBoolean("allMorphology");
    final String multiwords = Boolean.toString(this.parsedArguments
        .getBoolean("multiwords"));
    final String dictag = Boolean.toString(this.parsedArguments
        .getBoolean("dictag"));
    String outputFormat = parsedArguments.getString("outputFormat");
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

    final KAFDocument kaf = KAFDocument.createFromStream(breader);
    // language
    String lang;
    if (this.parsedArguments.getString("language") != null) {
      lang = this.parsedArguments.getString("language");
      if (!kaf.getLang().equalsIgnoreCase(lang)) {
        System.err.println("Language parameter in NAF and CLI do not match!!");
      }
    } else {
      lang = kaf.getLang();
    }
    final Properties properties = setAnnotateProperties(model, lemmatizerModel,
        lang, multiwords, dictag);
    final Annotate annotator = new Annotate(properties);
    final KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
        "terms", "ixa-pipe-pos-" + Files.getNameWithoutExtension(model),
        this.version + "-" + this.commit);
    newLp.setBeginTimestamp();

    if (allMorphology) {
      if (outputFormat.equalsIgnoreCase("conll")) {
        bwriter.write(annotator.getAllTagsLemmasToCoNLL(kaf));
      } else {
        annotator.getAllTagsLemmasToNAF(kaf);
        newLp.setEndTimestamp();
        bwriter.write(kaf.toString());
      }
    } else {
      if (outputFormat.equalsIgnoreCase("conll")) {
        bwriter.write(annotator.annotatePOSToCoNLL(kaf));
      } else {
        annotator.annotatePOSToKAF(kaf);
        newLp.setEndTimestamp();
        bwriter.write(kaf.toString());
      }
    }
    bwriter.close();
    breader.close();
  }

  /**
   * Generate the annotation parameter of the CLI.
   */
  private void loadAnnotateParameters() {
    this.annotateParser.addArgument("-m", "--model")
        .required(true)
        .help("It is required to provide a POS tagging model.");
    this.annotateParser.addArgument("-lm", "--lemmatizerModel")
         .required(true)
         .help("It is required to provide a lemmatizer model.");
    this.annotateParser.addArgument("-l", "--language")
        .choices("ca", "de", "en", "es", "eu", "fr", "gl", "it", "nl", "pt", "ru")
        .required(false)
        .help("Choose a language.");
    annotateParser.addArgument("-o", "--outputFormat")
        .required(false)
        .choices("naf", "conll")
        .setDefault("naf")
        .help("Choose output format; it defaults to NAF.\n");
    this.annotateParser.addArgument("-mw", "--multiwords")
        .action(Arguments.storeTrue())
        .help("Use to detect and process multiwords.\n");
    this.annotateParser.addArgument("-c","--clitics")
         .action(Arguments.storeTrue())
         .help("Detect and analyze clitic pronouns and contractions.\n");
    this.annotateParser.addArgument("-d", "--dictag")
        .action(Arguments.storeTrue())
        .help("Post process POS tagger output with a monosemic dictionary.\n");
    this.annotateParser.addArgument("-a","--allMorphology")
        .action(Arguments.storeTrue())
        .help("Print all the POS tags and lemmas before disambiguation.\n");
  }
  
  /**
   * Set up the TCP socket for annotation.
   */
  public final void server() {

    // load parameters into a properties
    String port = parsedArguments.getString("port");
    String model = parsedArguments.getString("model");
    String lemmatizerModel = parsedArguments.getString("lemmatizerModel");
    final String allMorphology = Boolean.toString(this.parsedArguments.getBoolean("allMorphology"));
    final String multiwords = Boolean.toString(this.parsedArguments
        .getBoolean("multiwords"));
    final String dictag = Boolean.toString(this.parsedArguments
        .getBoolean("dictag"));
    String outputFormat = parsedArguments.getString("outputFormat");
    // language parameter
    String lang = parsedArguments.getString("language");
    Properties serverproperties = setServerProperties(port, model, lemmatizerModel, lang, multiwords, dictag, outputFormat, allMorphology);
    new StatisticalTaggerServer(serverproperties);
  }
  
  /**
   * The client to query the TCP server for annotation.
   * 
   * @param inputStream
   *          the stdin
   * @param outputStream
   *          stdout
   */
  public final void client(final InputStream inputStream,
      final OutputStream outputStream) {

    String host = parsedArguments.getString("host");
    String port = parsedArguments.getString("port");
    try (Socket socketClient = new Socket(host, Integer.parseInt(port));
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
            System.in, "UTF-8"));
        BufferedWriter outToUser = new BufferedWriter(new OutputStreamWriter(
            System.out, "UTF-8"));
        BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(
            socketClient.getOutputStream(), "UTF-8"));
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
            socketClient.getInputStream(), "UTF-8"));) {

      // send data to server socket
      StringBuilder inText = new StringBuilder();
      String line;
      while ((line = inFromUser.readLine()) != null) {
        inText.append(line).append("\n");
      }
      inText.append("<ENDOFDOCUMENT>").append("\n");
      outToServer.write(inText.toString());
      outToServer.flush();
      
      // get data from server
      StringBuilder sb = new StringBuilder();
      String kafString;
      while ((kafString = inFromServer.readLine()) != null) {
        sb.append(kafString).append("\n");
      }
      outToUser.write(sb.toString());
    } catch (UnsupportedEncodingException e) {
      //this cannot happen but...
      throw new AssertionError("UTF-8 not supported");
    } catch (UnknownHostException e) {
      System.err.println("ERROR: Unknown hostname or IP address!");
      System.exit(1);
    } catch (NumberFormatException e) {
      System.err.println("Port number not correct!");
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Create the available parameters for POS tagging.
   */
  private void loadServerParameters() {
    serverParser.addArgument("-p", "--port").required(true)
        .help("Port to be assigned to the server.\n");
    serverParser.addArgument("-m", "--model").required(true)
        .help("It is required to provide a model to perform POS tagging.");
    this.serverParser.addArgument("-lm", "--lemmatizerModel")
        .required(true)
        .help("It is required to provide a lemmatizer model.");
    serverParser.addArgument("-l", "--language")
        .choices("ca", "de", "en", "es", "eu", "fr", "gl", "it", "nl", "pt", "ru")
        .required(true)
        .help("Choose a language to perform annotation with ixa-pipe-pos.");
    serverParser.addArgument("-o", "--outputFormat").required(false)
        .choices("naf", "conll")
        .setDefault(Flags.DEFAULT_OUTPUT_FORMAT)
        .help("Choose output format; it defaults to NAF.\n");
    serverParser.addArgument("-mw", "--multiwords")
        .action(Arguments.storeTrue())
        .help("Use to detect and process multiwords.\n");
    serverParser.addArgument("-d", "--dictag")
        .action(Arguments.storeTrue())
        .help("Post process POS tagger output with a monosemic dictionary.\n");
    serverParser.addArgument("-a","--allMorphology")
        .action(Arguments.storeTrue())
        .help("Print all the POS tags and lemmas before disambiguation.\n");
  }
  
  private void loadClientParameters() {
    
    clientParser.addArgument("-p", "--port")
        .required(true)
        .help("Port of the TCP server.\n");
    clientParser.addArgument("--host")
        .required(false)
        .setDefault(Flags.DEFAULT_HOSTNAME)
        .help("Hostname or IP where the TCP server is running.\n");
  }

  /**
   * Generate Properties objects for CLI usage.
   * @param model the model to perform the annotation
   * @param language the language
   * @param multiwords whether multiwords are to be detected
   * @param dictag whether tagging from a dictionary is activated
   * @return a properties object
   */
  private Properties setAnnotateProperties(final String model, final String lemmatizerModel,
      final String language, final String multiwords,
      final String dictag) {
    final Properties annotateProperties = new Properties();
    annotateProperties.setProperty("model", model);
    annotateProperties.setProperty("lemmatizerModel", lemmatizerModel);
    annotateProperties.setProperty("language", language);
    annotateProperties.setProperty("multiwords", multiwords);
    annotateProperties.setProperty("dictag", dictag);
    return annotateProperties;
  }
  
  private Properties setServerProperties(String port, String model, String lemmatizerModel, String language, String multiwords, String dictag, String outputFormat, String allMorphology) {
    Properties serverProperties = new Properties();
    serverProperties.setProperty("port", port);
    serverProperties.setProperty("model", model);
    serverProperties.setProperty("lemmatizerModel", lemmatizerModel);
    serverProperties.setProperty("language", language);
    serverProperties.setProperty("ruleBasedOption", multiwords);
    serverProperties.setProperty("dictTag", dictag);
    serverProperties.setProperty("outputFormat", outputFormat);
    serverProperties.setProperty("allMorphology", allMorphology);
    return serverProperties;
  }

}
