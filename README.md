
ixa-pipe-pos
============

ixa-pipe-pos is a Part of Speech tagger for English and Spanish. 
ixa-pipe-pos is part of IXA pipes, a multilingual NLP pipeline developed 
by the IXA NLP Group [http://ixa2.si.ehu.es/ixa-pipes]. 

Please go to [http://ixa2.si.ehu.es/ixa-pipes] for general information about the IXA
pipes tools but also for **official releases, including source code and binary
packages for all the tools in the IXA pipes toolkit**.

This document is intended to be the **usage guide of ixa-pipe-pos**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

## TABLE OF CONTENTS 

1. [Overview of ixa-pipe-pos](#overview)
  + [Distributed resources](#models)
2. [Usage of ixa-pipe-pos](#usage)
  + [POS tagging/lemmatizing](#tagging)
  + [Training your own models](#training)
  + [Evaluation](#evaluation)

## OVERVIEW

ixa-pipe-pos provides POS tagging and lemmatization for English and Spanish. We
provide two fast POS tagging models: 

+ **POS tagging models for English** trained and evaluated using the WSJ treebank as explained 
  in K. Toutanova, D. Klein, and C. D. Manning. Feature-rich part-of-speech tagging with a cyclic 
  dependency network. In Proceedings of HLT-NAACL’03, 2003. 
+ **POS tagging models for Spanish** trained and evaluated using the Ancora corpus; it was randomly
  divided in 90% for training (450K words) and 10% testing (50K words). 
+ **Dictionary-based lemmatization** for English and Spanish. 

For this first release we provide two reasonably fast POS tagging models based on the Perceptron (Collins 2002) and 
Maximum Entropy (Ratnaparkhi 1999) algorithms. To avoid duplication of efforts, we use the machine learning API 
provided by the [Apache OpenNLP project](http://opennlp.apache.org). Additionally, we have added dictionary-based lemmatization. 

### Models

The following resources are provided in the [pos-resources.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/pos-resources.tgz) package: 

+ **English POS Models**:
  + Penn Treebank: **en-pos-perceptron-c0-b3-dev.bin**: 97.06

+ **Spanish POS Models**: we obtained better results overall with Maximum Entropy
  models (Ratnapharki 1999). The best results are obtained when a c0 (cutoff 0)
  is used, but those models are slower for production than the Perceptron
  models. Therefore, we provide both types, based on maxent and perceptron.
  + Ancora: **es-pos-maxent-750-c0-b3.bin**: 98.88 Word accuracy.
  + Ancora: **es-pos-perceptron-c0-b3.bin**: 98.24 Word accuracy (**this is the default**). 

Furthermore, the following resources **are required for lemmatization**, available in the [lemmatizer-dicts.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/lemmatizer-dicts.tgz)
package. Note that the dictionaries come with their own licences, please do comply with them:

+ **Lemmatizer Dictionaries**:
  + **English**:
    + **Plain text dictionary**: en-lemmatizer.dict is a "word lemma postag" dictionary in plain text to perform lemmatization.
    + **Morfologik-stemming**: english.dict is the same as en-lemmas.dict but binarized as a finite state automata. 
      using the morfologik-stemming project (see NOTICE file for details). This method uses 10% of RAM with respect to the plain text dictionary (**this is the default**).
  + **Spanish**:
    + **Plain text dictionary**: es-lemmatizer.dict.
    + **Morfologik stemming**: spanish.dict.

ixa-pipe-pos is distributed under Apache License version 2.0 (see LICENSE.txt for details).

## USAGE

ixa-pipe-pos provides 3 basic functionalities:

1. **tag**: reads a NAF document containing *wf* and *term* elements and tags named
   entities.
2. **train**: trains new model for English or Spanish with several options
   available.
3. **eval**: evaluates a trained model with a given test set.

Each of these functionalities are accessible by adding (tag|train|eval) as a
subcommand to ixa-pipe-pos-$version.jar. Please read below and check the -help
parameter: 

````shell
java -jar target/ixa-pipe-pos-$version.jar (tag|train|eval) -help
````

### POS Tagging with ixa-pipe-pos

If you are in hurry, just execute: 

````shell
cat file.txt | ixa-pipe-tok | java -jar $PATH/target/ixa-pipe-pos-$version.jar tag
````

If you want to know more, please follow reading.

ixa-pipe-pos reads NAF documents (with *wf* and *term* elements) via standard input and outputs NAF
through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

You can get the necessary input for ixa-pipe-pos by piping it with 
[ixa-pipe-tok](https://github.com/ixa-ehu/ixa-pipe-tok). 

There are several options to tag with ixa-pipe-pos: 

+ **lang**: choose between en and es. If no language is chosen, the one specified
  in the NAF header will be used.
+ **model**: provide the model to do the tagging. If no model is provided via
  this parameter, ixa-pipe-pos will revert to the baseline model distributed
  in the release.  
+ **beamsize**: choose beam size for decoding. There is no definitive evidence
  that using larger or smaller beamsize actually improves accuracy. It is known
  to slow things down considerably if beamsize is set to 100, for example.
+ **lemmatize**: choose dictionary method to perform lemmatization:
  + **bin**: Morfologik binary dictionary (**default**).
  + **plain**: plain text dictionary.

**Tagging Example**: 

````shell
cat file.txt | ixa-pipe-tok | java -jar $PATH/target/ixa-pipe-pos-$version.jar tag
````

### Training new models

The following options are available via the train subcommand:

+ **features**: currently we provide 2 local feature sets plus non-local
features using lexicons:
  + **opennlp**: Apache OpenNLP featureset, kept for compatibility.
  + **baseline**: local features adding bigrams and trigrams.
  + **autoDict**: pass this parameter to automatically build a tag dictionary
  from the training data.
  + **dictPath**: pass this parameter to use an already existing tag dictionary
  Check Apache OpenNLP documentation for the dictionary format.
+ **input**: the training dataset.
+ **testSet**: self-explanatory, the test dataset.
+ **devSet**: the development set if cross evaluation is chosen to find the
  right number of iterations (this option is still very experimental).
+ **output**: the model name resulting of the training. If not output is
  chosen, ixa-pipe-pos will save the model in a file named following the
  features used.
+ **params**: this is where most of the training options are specified.
  + **Algorithm**: choose between PERCEPTRON or MAXENT.
  + **Iterations**: choose number of iterations.
  + **Cutoff**: consider only events above the cutoff number specified.
  + **Threads**: multi-threading, only works with MAXENT.
  + **Language**: en or es. 
  + **Beamsize**: choose beamsize for decoding. It defaults to 3.
  + **Corpus**: corpus format. Currently opennlp native format supported.
  + **CrossEval**: choose the range of iterations at which to perform
  evaluation. This parameter tells the trainer to find the best number of
  iterations for MAXENT training on a development set. Then that iteration
  number will be used to train the final model. In a very experimental state. 
+ autoDict: automatically generate tag dictionary from training data
+ dictPath: use an already existing tag dictionary to train
**Example**:

````shell
java -jar target/ixa.pipe.pos-$version.jar train -f baseline -p trainParams.txt -i train.data -t test.data -o test-pos.bin
````

### Evaluation

To evaluate a trained model, the eval subcommand provides the following
options: 

+ **model**: input the name of the model to evaluate.
+ **testSet**: testset to evaluate the model.
+ **evalReport**: choose the detail in displaying the results: 
  + **brief**: it just prints the word accuracy.
  + **detailed**: detailed report with confusion matrixes and so on. 
  + **error**: print to stderr all the false positives.
+ **corpus**: choose between native opennlp and conll 2003 formats.
+ **beamsize**: choose beamsize for decoding.

**Example**:

````shell
java -jar target/ixa.pipe.nerc-$version.jar eval -m test-pos.bin -l en -t test.data 
````

## JAVADOC

It is possible to generate the javadoc of the module by executing:

````shell
cd ixa-pipe-pos/
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-pos-$version-javadoc.jar

## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories


## INSTALLATION

Installing the ixa-pipe-pos requires the following steps:

If you already have installed in your machine the Java 1.7+ and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

### 1. Install JDK 1.7

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java7
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.7

### 2. Install MAVEN 3

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 7 that is using.

### 3. Get module source code

If you must get the module source code from here do this:

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-pos
````

### 4. Download the Resources

You will need to download the trained models and other resources and copy them to ixa-pipe-pos/src/main/resources/
for the module to work properly:

Download the models and dictionaries and untar the archive into the src/main/resources directory:

````shell
cd ixa-pipe-pos/src/main/resources
wget http://ixa2.si.ehu.es/ixa-pipes/models/pos-resources.tgz
wget http://ixa2.si.ehu.es/ixa-pipes/models/lemmatizer-dicts.tgz
tar xvzf pos-resources.tgz
tar xvzf lemmatizer-dicts.tgz
````
The pos-resources contains the baseline models to which ixa-pipe-pos backs off if not model is provided as parameter for tagging.

### 5. Compile

````shell
cd ixa-pipe-pos
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-pos-$version.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

## Contact information

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````

