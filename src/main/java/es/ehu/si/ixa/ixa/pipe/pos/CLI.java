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

package es.ehu.si.ixa.ixa.pipe.pos;

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.postag.POSModel;
import opennlp.tools.util.TrainingParameters;

import org.jdom2.JDOMException;

import com.google.common.io.Files;

import es.ehu.si.ixa.ixa.pipe.pos.eval.CrossValidator;
import es.ehu.si.ixa.ixa.pipe.pos.eval.Evaluate;
import es.ehu.si.ixa.ixa.pipe.pos.train.FixedTrainer;
import es.ehu.si.ixa.ixa.pipe.pos.train.Flags;
import es.ehu.si.ixa.ixa.pipe.pos.train.InputOutputUtils;
import es.ehu.si.ixa.ixa.pipe.pos.train.Trainer;

/**
 * Main class of ixa-pipe-pos, the pos tagger of ixa-pipes
 * (ixa2.si.ehu.es/ixa-pipes). The annotate method is the main entry point.
 *
 * @author ragerri
 * @version 2014-11-30
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
  private final String commit = CLI.class.getPackage().getSpecificationVersion();
  /**
   * The CLI arguments.
   */
  private Namespace parsedArguments = null;
  /**
   * The argument parser.
   */
  private ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-pos-" + version + ".jar").description(
      "ixa-pipe-pos-" + version
          + " is a multilingual POS tagger developed by IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private Subparsers subParsers = argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private Subparser annotateParser;
  /**
   * The parser that manages the training sub-command.
   */
  private Subparser trainParser;
  /**
   * The parser that manages the evaluation sub-command.
   */
  private Subparser evalParser;
  /**
   * The parser that manages the cross validation sub-command.
   */
  private Subparser crossValidateParser;
  /**
   * Default beam size for decoding.
   */
  public static final String DEFAULT_BEAM_SIZE = "3";

  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    annotateParser = subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
    trainParser = subParsers.addParser("train").help("Training CLI");
    loadTrainingParameters();
    evalParser = subParsers.addParser("eval").help("Evaluation CLI");
    loadEvalParameters();
    crossValidateParser = subParsers.addParser("cross").help("Cross validation CLI");
    loadCrossValidateParameters();
  }

  /**
   * The main method.
   *
   * @param args
   *          the arguments
   * @throws IOException
   *           the input output exception if not file is available
   * @throws JDOMException
   *           as the input is a NAF file, a JDOMException could be thrown
   */
  public static void main(final String[] args) throws IOException,
      JDOMException {

    CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command interface parameters with the argParser.
   *
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if problems with the incoming data
   * @throws JDOMException 
   * @throws JWNLException
   */
  public final void parseCLI(final String[] args) throws IOException, JDOMException {
    try {
      parsedArguments = argParser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
      if (args[0].equals("tag")) {
        annotate(System.in, System.out);
      } else if (args[0].equals("eval")) {
        eval();
      } else if (args[0].equals("train")) {
        train();
      }  else if (args[0].equals("cross")) {
        crossValidate();
      }
    } catch (ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-pos-" + version
          + ".jar (tag|train|eval|cross) -help for details");
      System.exit(1);
    }
  }

  /**
   * Main entry point for annotation. Takes system.in as input and outputs
   * annotated text via system.out.
   *
   * @param inputStream
   *          the input stream
   * @param outputStream
   *          the output stream
   * @throws IOException
   *           the exception if not input is provided
   */
  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException, JDOMException {

    String model = parsedArguments.getString("model");
    String beamSize = parsedArguments.getString("beamSize");
    String multiwords = Boolean.toString(parsedArguments.getBoolean("multiwords"));
    String dictag = Boolean.toString(parsedArguments.getBoolean("dictag"));
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

    KAFDocument kaf = KAFDocument.createFromStream(breader);
    //language
    String lang;
    if (parsedArguments.getString("language") != null) {
      lang = parsedArguments.getString("language");
      if (!kaf.getLang().equalsIgnoreCase(lang)) {
        System.err
            .println("Language parameter in NAF and CLI do not match!!");
        System.exit(1);
      }
    } else {
      lang = kaf.getLang();
    }
    Properties properties = setAnnotateProperties(model, lang, beamSize, multiwords, dictag);
    Annotate annotator = new Annotate(properties);
    if (parsedArguments.getBoolean("nokaf")) {
      bwriter.write(annotator.annotatePOSToCoNLL(kaf));
    } else {
      KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
          "terms", "ixa-pipe-pos-" + Files.getNameWithoutExtension(model), version + "-" + commit);
      newLp.setBeginTimestamp();
      annotator.annotatePOSToKAF(kaf);
      newLp.setEndTimestamp();
      bwriter.write(kaf.toString());
      } 
    bwriter.close();
    breader.close();
  }

  /**
   * Generate the annotation parameter of the CLI.
   */
  private void loadAnnotateParameters() {
    annotateParser.addArgument("-m", "--model")
        .required(true)
        .help("It is required to provide a model to perform POS tagging.");
    annotateParser.addArgument("-l", "--lang")
        .choices("en", "es", "gl", "it")
        .required(false)
        .help("Choose a language to perform annotation with ixa-pipe-pos.");
   
    annotateParser.addArgument("--beamSize")
        .required(false)
        .setDefault(DEFAULT_BEAM_SIZE)
        .help("Choose beam size for decoding, it defaults to 3.");
    annotateParser
        .addArgument("--nokaf")
        .action(Arguments.storeTrue())
        .help(
            "Do not print tokens in NAF format, but conll tabulated format.\n");
    annotateParser.addArgument("-mw", "--multiwords")
        .action(Arguments.storeTrue())
        .help("Use to detect and process multiwords.\n");
    annotateParser.addArgument("-d", "--dictag")
        .action(Arguments.storeTrue())
        .help("Post process POS tagger output with a monosemic dictionary.\n");
  }

  /**
   * Main entry point for training.
   *
   * @throws IOException
   *           throws an exception if errors in the various file inputs.
   */
  public final void train() throws IOException {
	// load training parameters file
	String paramFile = parsedArguments.getString("params");
	TrainingParameters params = InputOutputUtils
	        .loadTrainingParameters(paramFile);
    String outModel = null;
    if (params.getSettings().get("OutputModel") == null || params.getSettings().get("OutputModel").length() == 0) {
        outModel = Files.getNameWithoutExtension(paramFile) + ".bin";
        params.put("OutputModel", outModel);
      }
      else {
        outModel = Flags.getModel(params);
      }
    Trainer posTaggerTrainer = new FixedTrainer(params);
    POSModel trainedModel = posTaggerTrainer.train(params);
    CmdLineUtil.writeModel("ixa-pipe-pos", new File(outModel), trainedModel);
  }

  /**
   * Loads the parameters for the training CLI.
   */
  private final void loadTrainingParameters() {
	  trainParser.addArgument("-p", "--params")
	   .required(true)
      .help("Load the training parameters file\n");
  }

  /**
   * Main entry point for evaluation.
   * @throws IOException the io exception thrown if
   * errors with paths are present
   */
  public final void eval() throws IOException {
    String testFile = parsedArguments.getString("testSet");
    String model = parsedArguments.getString("model");
    String beamSize = parsedArguments.getString("beamSize");

    Evaluate evaluator = new Evaluate(testFile, model, beamSize);
    if (parsedArguments.getString("evalReport") != null) {
      if (parsedArguments.getString("evalReport").equalsIgnoreCase("detailed")) {
        evaluator.detailEvaluate();
      } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
          "error")) {
        evaluator.evalError();
      } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
          "brief")) {
        evaluator.evaluate();
      }
    } else {
      evaluator.evaluate();
    }
  }

  /**
   * Load the evaluation parameters of the CLI.
   */
  private final void loadEvalParameters() {
    evalParser.addArgument("-m", "--model")
         .required(true)
        .help("Choose model");
    evalParser.addArgument("-t", "--testSet")
        .required(true)
        .help("Input testset for evaluation");
    evalParser.addArgument("--evalReport")
        .required(false)
        .choices("brief", "detailed", "error")
        .help("Choose type of evaluation report; defaults to brief");
    evalParser.addArgument("--beamSize")
        .setDefault(DEFAULT_BEAM_SIZE)
        .type(Integer.class)
        .help("Choose beam size for evaluation: 1 is faster.");
  }
  
  /**
   * Main access to the cross validation.
   * 
   * @throws IOException
   *           input output exception if problems with corpora
   */
  public final void crossValidate() throws IOException {

    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    CrossValidator crossValidator = new CrossValidator(params);
    crossValidator.crossValidate(params);
  }
  
  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadCrossValidateParameters() {
    crossValidateParser.addArgument("-p", "--params").required(true)
        .help("Load the Cross validation parameters file\n");
  }
  
  /**
   * Set a Properties object with the CLI parameters for annotation.
   * @param model the model parameter
   * @param language language parameter
   * @param beamSize the beamsize decoding
   * @param lemmatize the lemmatization method
   * @return the properties object
   */
  private Properties setAnnotateProperties(String model, String language, String beamSize, String multiwords, String dictag) {
    Properties annotateProperties = new Properties();
    annotateProperties.setProperty("model", model);
    annotateProperties.setProperty("language", language);
    annotateProperties.setProperty("beamSize", beamSize);
    annotateProperties.setProperty("multiwords", multiwords);
    annotateProperties.setProperty("dictag", dictag);
    return annotateProperties;
  }

}
