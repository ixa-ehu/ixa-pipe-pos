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
package eus.ixa.ixa.pipe.lemma.train;

import java.io.IOException;

import eus.ixa.ixa.pipe.lemma.LemmatizerFactory;
import eus.ixa.ixa.pipe.pos.train.Flags;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.TrainingParameters;

/**
 * FixedTrainer to train the lemmatizer.
 * 
 * @author ragerri
 * @version 2016-01-28
 */
public class LemmatizerFixedTrainer extends AbstractLemmatizerTrainer {

  /**
   * Extends the {@code AbstractLemmatizerTrainer} providing some {@code LemmatizerFactory}
   * .
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exception
   */
  public LemmatizerFixedTrainer(final TrainingParameters params) throws IOException {
    super(params);
    setLemmatizerFactory(getTrainerFactory(params));
  }

  /**
   * Instantiate the {@code LemmatizerFactory} according to the features
   * specified in the parameters properties file.
   * 
   * @param params
   *          the training parameters
   * @return the factory
   */
  private final LemmatizerFactory getTrainerFactory(
      final TrainingParameters params) {
    LemmatizerFactory lemmatizerFactory = null;
    final String featureSet = Flags.getFeatureSet(params);
    if (featureSet.equalsIgnoreCase("chunk")) {
      try {
        lemmatizerFactory = LemmatizerFactory.create(
            LemmatizerFactory.class.getName());
      } catch (final InvalidFormatException e) {
        e.printStackTrace();
      }
    } else {
      try {
        lemmatizerFactory = LemmatizerFactory.create(
            LemmatizerFactory.class.getName());
      } catch (final InvalidFormatException e) {
        e.printStackTrace();
      }
    }
    return lemmatizerFactory;
  }

}

