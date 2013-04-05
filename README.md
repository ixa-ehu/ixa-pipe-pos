
IXA-pipe-pos
============

This module uses Apache OpenNLP programatically to perform POS tagging.
Models for English have been trained using the WSJ treebank (performance 96.48%).
This module is part of IXA-Pipeline ("is a pipeline"), a multilingual NLP pipeline
developed by the IXA NLP Group (ixa.si.ehu.es).

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

If you already have installed in your machine JDK6 and MAVEN 3, please go to step 3
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

ixa-pipe-tok original repo is hosted at Bitbucket, and can be cloned as follows:

````shell
hg clone ssh://hg@bitbucket.org/ragerri/ixa-pipe-tok
````

If you are a github user, we provide a github mirror of the original repo:

````shell
git clone git@github.com:ragerri/ixa-pipe-tok.git
````

4. Download models and other resources
--------------------------------------

The POS tagger needs the trained models to work properly. They can be downloaded from

````shell
http://ixa3.si.ehu.es/~ragerri/ixa-pipeline-models/
````

Two models are to be copied to ixa-pipe-pos/src/main/resources/: en-pos-perceptron-1000-dev.bin, and es-pos-perceptron-1000-dev.bin
Note that if you change the name of the models the source code in Models.java will need to be modified accordingly.


To perform English lemmatization the module currently uses WordNet-3.0. You will need to download WordNet and provide $WordNet/dict as
a value of the -w option when running ixa-pipe-pos (see point 7. below).

````shell
wget http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz
````

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

http://kyoto-project.eu/www2.let.vu.nl/twiki/pub/Kyoto/TechnicalPapers/WP002_TR009_KAF_Framework.pdf

You can get the tokenized input for this module from ixa-pipe-tok. To run the program execute:

````shell
cat wordforms.kaf | java -jar $PATH/target/ixa-pipe-pos-1.0.jar -l $lang -w $wn30/dict
````

Current paramaters for specifying the language (to load the relevant models) and the location
of the WordNet/dict directory (required for lemmatization) are mandatory.

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
