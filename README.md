
IXA-pipe-pos
============

ixa-pipe-pos provides POS tagging and lemmatization for English and Spanish.
This module is part of IXA-Pipeline ("is a pipeline"), a multilingual NLP pipeline
developed by the IXA NLP Group (ixa.si.ehu.es).

- POS tagging models have been trained using the Apache OpenNLP API:

    + English perceptron models have been trained and evaluated using the WSJ treebank as explained in
      K. Toutanova, D. Klein, and C. D. Manning. Feature-rich part-of-speech tagging with a cyclic dependency network. In Proceedings of HLT-NAACL’03, 2003. Currently we obtain a performance of 96.48% vs 97.24% obtained by Toutanova et al. (2003).

    + Spanish Maximum Entropy models have been trained and evaluated using the Ancora corpus; it was randomly
  divided in 90% for training (440K words) and 10% testing (70K words), obtaining a performance of 98.88%.

- Lemmatization is dictionary based:

    + English:
        + WordNet-3.0. You will need to download WordNet and provide $WordNet/dict as a value of the -w option when running ixa-pipe-pos (see point 7. below).
        + Plain text dictionary: en-lemmas.dict is a "Word POStag lemma" dictionary in plain text to perform lemmatization.
        + Morfologik-stemming: english.dict is the same as en-lemmas.dict but binarized as a finite state automata using the morfologik-stemming project (see NOTICE file for details) This method uses 10% of RAM with respect to the plain text dictionary and works noticeably faster.

    + Spanish:
        + Plain text dictionary: es-lemmas.dict.
        + Morfologik stemming: spanish.dict.

To get WordNet go to:

````shell
wget http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz
````

By default lemmatization for both English and Spanish is performed using the Morfologik-stemming binary dictionaries.

Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories



INSTALLATION
============

Installing the ixa-pipe-pos requires the following steps:

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.6
-------------------

If you do not install JDK 1.6 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java6
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java16
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.6

2. Install MAVEN 3
------------------

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

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

3. Get module source code
--------------------------

````shell
git clone git@github.com:ixa-ehu/ixa-pipe-tok.git
````

4. Download models and other resources
--------------------------------------

The POS tagger needs the trained models and dictionaries to do the lemmatization. Download the models
and untar the archive into the src/main/resources directory:

````shell
cd ixa-pipe-pos/src/main/resources
wget http://ixa2.si.ehu.es/ragerri/ixa-pipeline-models/pos-resources.tgz
tar xvzf pos-resources.tgz
````
If you change the name of the models you will need to modify also the source code in Models.java.

To perform English lemmatization the module uses three different methods for English and two for Spanish:


5. Move into main directory
---------------------------

````shell
cd ixa-pipe-pos
````

6. Install module using maven
-----------------------------

````shell
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-pos-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module in the local maven repository, usually located at ~/.m2/, execute:

````shell
mvn clean install
````

7. USING ixa-pipe-pos
=====================

The program accepts tokenized text in KAF format as standard input and outputs KAF.

https://github.com/opener-project/kaf/wiki/KAF-structure-overview

You can get the tokenized input for this module from ixa-pipe-tok. To run the program execute:

````shell
cat wordforms.kaf | java -jar $PATH/target/ixa-pipe-pos-1.0.jar
````

See

````shell
java -jar $PATH/target/ixa-pipe-pos-1.0.jar -help
````

for more options running the module such as lemmatization methods.


GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file target/ixa-pipe-pos-1.0-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````
