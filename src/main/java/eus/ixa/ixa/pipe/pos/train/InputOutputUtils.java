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

package eus.ixa.ixa.pipe.pos.train;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.ml.TrainerFactory;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

/**
 * Utility functions to read and save ObjectStreams.
 * 
 * @author ragerri
 * @version 2014-07-08
 * 
 */
public final class InputOutputUtils {

  /**
   * Private constructor. This class should only be used statically.
   */
  private InputOutputUtils() {

  }

  /**
   * Check input file integrity.
   * 
   * @param name
   *          the name of the file
   * @param inFile
   *          the file
   */
  private static void checkInputFile(final String name, final File inFile) {

    String isFailure = null;

    if (inFile.isDirectory()) {
      isFailure = "The " + name + " file is a directory!";
    } else if (!inFile.exists()) {
      isFailure = "The " + name + " file does not exist!";
    } else if (!inFile.canRead()) {
      isFailure = "No permissions to read the " + name + " file!";
    }

    if (null != isFailure) {
      throw new TerminateToolException(-1, isFailure + " Path: "
          + inFile.getAbsolutePath());
    }
  }

  /**
   * Load the parameters in the {@code TrainingParameters} file.
   * 
   * @param paramFile
   *          the training parameters file
   * @return default loading of the parameters
   */
  public static TrainingParameters loadTrainingParameters(final String paramFile) {
    return loadTrainingParameters(paramFile, false);
  }

  /**
   * Load the parameters in the {@code TrainingParameters} file.
   * 
   * @param paramFile
   *          the parameter file
   * @param supportSequenceTraining
   *          wheter sequence training is supported
   * @return the parameters
   */
  private static TrainingParameters loadTrainingParameters(
      final String paramFile, final boolean supportSequenceTraining) {

    TrainingParameters params = null;

    if (paramFile != null) {

      checkInputFile("Training Parameter", new File(paramFile));

      InputStream paramsIn = null;
      try {
        paramsIn = new FileInputStream(new File(paramFile));

        params = new opennlp.tools.util.TrainingParameters(paramsIn);
      } catch (final IOException e) {
        throw new TerminateToolException(-1,
            "Error during parameters loading: " + e.getMessage(), e);
      } finally {
        try {
          if (paramsIn != null) {
            paramsIn.close();
          }
        } catch (final IOException e) {
          System.err.println("Error closing the input stream");
        }
      }

      if (!TrainerFactory.isValid(params.getSettings())) {
        throw new TerminateToolException(1, "Training parameters file '"
            + paramFile + "' is invalid!");
      }
    }

    return params;
  }

  /**
   * Read the file into an {@code ObjectStream}.
   * 
   * @param infile
   *          the string pointing to the file
   * @return the object stream
   */
  public static ObjectStream<String> readFileIntoMarkableStreamFactory(
      final String infile) {

    InputStreamFactory inputStreamFactory = null;
    try {
      inputStreamFactory = new MarkableFileInputStreamFactory(new File(infile));
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
    ObjectStream<String> lineStream = null;
    try {
      lineStream = new PlainTextByLineStream(inputStreamFactory, "UTF-8");
    } catch (final IOException e) {
      CmdLineUtil.handleCreateObjectStreamError(e);
    }
    return lineStream;
  }

}
