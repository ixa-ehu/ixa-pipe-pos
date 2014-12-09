
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
  + [Distributed resources](#resources)
  + [Distributed models](#models)
2. [Usage of ixa-pipe-pos](#usage)
  + [POS tagging/lemmatizing](#tagging)
  + [Training your own models](#training)
  + [Evaluation](#evaluation)
3. [API via Maven Dependency](#api)
4. [Git installation](#installation)
5. [Adding your language](#extend)

## OVERVIEW

ixa-pipe-pos provides POS tagging and lemmatization for English and Spanish. We
provide two fast POS tagging models: 

+ **POS tagging models for English** trained and evaluated using the WSJ treebank as explained 
  in K. Toutanova, D. Klein, and C. D. Manning. Feature-rich part-of-speech tagging with a cyclic 
  dependency network. In Proceedings of HLT-NAACL’03, 2003. 
+ **POS tagging models for Spanish** trained and evaluated using the Ancora corpus via 5-fold and 10-fold cross-validation.
+ **Dictionary-based lemmatization** for English and Spanish. 

For this first release we provide two reasonably fast POS tagging models based on the Perceptron (Collins 2002) and 
Maximum Entropy (Ratnaparkhi 1999) algorithms. To avoid duplication of efforts, we use and contribute to the machine learning API provided by the [Apache OpenNLP project](http://opennlp.apache.org). Additionally, we have added other features such as dictionary-based lemmatization, multiword and clitic pronoun treatment, etc, as described below.

ixa-pipe-pos is distributed under Apache License version 2.0 (see LICENSE.txt for details).

### Resources

**The contents of this package are required for compilation**. Therefore, please get and **unpack** the contents of 
this tarball in the **src/main/resources/** directory inside ixa-pipe-pos.

The following resources **include lemmatization and multiword dictionaries**, and are available in the [pos-resources.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/pos-resources.tgz)
package. Note that the dictionaries come with their own licences, please do comply with them:

+ **Lemmatizer Dictionaries**:
  + **English**:
    + **Plain text dictionary**: en-lemmatizer.dict is a "word\tlemma\tpostag" dictionary in plain text to perform lemmatization.
    + **Morfologik-stemming**: english.dict is the same as en-lemmas.dict but binarized as Finite State Automata. 
      using the morfologik-stemming project (see NOTICE file for details). **this is the default for every language**
  + **Spanish**:
    + **Plain text dictionary**: es-lemmatizer.dict.
    + **Morfologik stemming**: spanish.dict.
  + **Galician**:
    + **Plain text**: gl-lemmatizer.dict
    + **Morfologik stemming**: galician.dict

+ **Multiword Dictionaries**:
  + **Spanish**: es-locutions.dict contains a list of multiword expressions in Spanish.
  + **Galician**: gl-locutions.dict contains a list of multiword expressions in Galician.

To use the resources "as is" just download the package, copy it and untar it intto the src/main/resources directory. 

### Models

The following pre-trained models are provided in the [pos-models-$version.tgz](http://ixa2.si.ehu.es/ixa-pipes/models/pos-models-1.3.0.tgz) package: 

+ **English POS Models**:
  + Penn Treebank: **en-pos-perceptron-c0-b3-dev.bin**: 97.06

+ **Spanish POS Models**: We provide two Perceptron models:
  + Ancora with automatic dictionary created from training data **es-perceptron-baseline-autodict01-ancora.bin**: 97.56 word accuracy via 10-fold cross validation
  + Ancora: **es-perceptron-baseline-ancora.bin**: With Baseline features, this is slightly faster. 

Remember that for Spanish the output of the statistical models can be post-processed using the monosemic dictionaries provided via the **--dictag CLI option**.

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

### Tagging

If you are in hurry, just execute: 

````shell
cat file.txt | ixa-pipe-tok | java -jar $PATH/target/ixa-pipe-pos-$version.jar tag -m model.bin
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
+ **multiwords**: activates the multiword detection option.

**Tagging Example**: 

````shell
cat file.txt | ixa-pipe-tok | java -jar $PATH/target/ixa-pipe-pos-$version.jar tag -m model.bin
````

### Training

To train a new model, you just need to pass a training parameters file as an
argument. Every training option is documented in the template trainParams.prop file.

**Example**:

````shell
java -jar target/ixa.pipe.pos-$version.jar train -p trainParams.prop
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
java -jar target/ixa.pipe.pos-$version.jar eval -m test-pos.bin -l en -t test.data 
````

## API

The easiest way to use ixa-pipe-pos programatically is via Apache Maven. Add
this dependency to your pom.xml:

````shell
<dependency>
    <groupId>es.ehu.si.ixa</groupId>
    <artifactId>ixa-pipe-pos</artifactId>
    <version>1.3.0</version>
</dependency>
````

## JAVADOC

The javadoc of the module is located here:

````shell
ixa-pipe-pos/target/ixa-pipe-pos-$version-javadoc.jar
````
## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + trainParams.prop      A template properties file containing documention
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

You will need to download the resources and copy them to ixa-pipe-pos/src/main/resources/
for the module to work properly:

Download the resources and untar the archive into the src/main/resources directory:

````shell
cd ixa-pipe-pos/src/main/resources
wget http://ixa2.si.ehu.es/ixa-pipes/models/pos-resources.tgz
tar xvzf pos-resources.tgz
````
The pos-resources contains the required dictionaries for ixa-pipe-pos to run.

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

## Extend

To add your language to ixa-pipe-pos the following steps are required: 

+ Create lemmatizer and (if required) multiword dictionaries following the format of those distributed in **pos-resources.tgz**.
  + **Create binary dictionaries (FSA):** Starting from the plain text tabulated dictionaries, do the following steps:
    + Get Morfologik standalone binary: http://sourceforge.net/projects/morfologik/files/morfologik-stemming/
    + java -jar morfologik-tools-1.6.0-standalone.jar tab2morph --annotation "*" -i  ~/javacode/ixa-pipe-pos/pos-resources/lemmatizer-dicts/freeling/es-lemmatizer.dict -o spanish.morph
    + java -jar morfologik-tools-1.6.0-standalone.jar fsa_build -i spanish.morph -o spanish.dict
    + **Create a *.info file like spanish.info**
+ **Modify the classes** CLI, Resources and Annotate (and if multiword is required also MultiWordMatcher) adding for your language the same information that it is available for other languages.
+ Train a model. **It is important that the tagset of the dictionaries and corpus be the same**. Also it is recommended to train a model with an external dictionary (see tag-dicts dictionaries distributed in pos-resources tarball).
+ Add documentation to this README.md.
+ **Do a pull request** to merge the changes with your new language.
+ Send us the resources and models created if you want them to be distributed with ixa-pipe-pos (Apache License 2.0 is favoured).

## Contact information

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````

