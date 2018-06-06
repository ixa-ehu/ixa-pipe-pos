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
package eus.ixa.ixa.pipe.pos.train;

import java.io.IOException;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.TrainingParameters;

/**
 * FixedTrainer to train the pos tagger.
 * 
 * @author ragerri
 * @version 2014-11-26
 */
public class FixedTrainer extends AbstractTaggerTrainer {

  /**
   * Extends the {@code AbstractTrainer} providing some {@code POSTaggerFactory}
   * .
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exception
   */
  public FixedTrainer(final TrainingParameters params) throws IOException {
    super(params);

    final String dictPath = Flags.getDictionaryFeatures(params);
    setPosTaggerFactory(getTrainerFactory(params));
    createTagDictionary(dictPath);
    createAutomaticDictionary(getDictSamples(), getDictCutOff());

  }

  /**
   * Instantiate the {@code POSTaggerFactory} according to the features
   * specified in the parameters properties file.
   * 
   * @param params
   *          the training parameters
   * @return the factory
   */
  private final POSTaggerFactory getTrainerFactory(
      final TrainingParameters params) {
    POSTaggerFactory posTaggerFactory = null;
    final String featureSet = Flags.getFeatureSet(params);
    Dictionary ngramDictionary = null;
    if (Flags.getNgramDictFeatures(params) != Flags.DEFAULT_DICT_CUTOFF) {
      ngramDictionary = createNgramDictionary(getDictSamples(),
          getNgramDictCutOff());
    }
    if (featureSet.equalsIgnoreCase("Opennlp")) {
      try {
        posTaggerFactory = POSTaggerFactory.create(
            POSTaggerFactory.class.getName(), ngramDictionary, null);
      } catch (final InvalidFormatException e) {
        e.printStackTrace();
      }
    } else {
      try {
        posTaggerFactory = POSTaggerFactory.create(
            BaselineFactory.class.getName(), ngramDictionary, null);
      } catch (final InvalidFormatException e) {
        e.printStackTrace();
      }
    }
    return posTaggerFactory;
  }

}
