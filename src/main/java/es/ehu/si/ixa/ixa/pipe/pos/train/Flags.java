package es.ehu.si.ixa.ixa.pipe.pos.train;

import opennlp.tools.util.TrainingParameters;

public class Flags {

	public static final String DEFAULT_FEATURE_FLAG = "no";
	/**
	 * Default beam size for decoding.
	 */
	public static final int DEFAULT_BEAM_SIZE = 3;
	public static final int DEFAULT_FOLDS_VALUE = 10;
	public static final String DEFAULT_EVALUATE_MODEL = "off";
	public static final String DEFAULT_FEATURESET_FLAG = "Baseline";
	public static final String DEFAULT_DICT_PATH = "off";
	public static final String DEFAULT_EVAL_FORMAT = "opennlp";

	private Flags() {

	}

	public static String getLanguage(TrainingParameters params) {
		String lang = null;
		if (params.getSettings().get("Language") == null) {
			langException();
		} else {
			lang = params.getSettings().get("Language");
		}
		return lang;
	}

	public static String getDataSet(String dataset, TrainingParameters params) {
		String trainSet = null;
		if (params.getSettings().get(dataset) == null) {
			datasetException();
		} else {
			trainSet = params.getSettings().get(dataset);
		}
		return trainSet;
	}

	public static String getModel(TrainingParameters params) {
		String model = null;
		if (params.getSettings().get("OutputModel") == null) {
			modelException();
		} else if (params.getSettings().get("OutputModel") != null
				&& params.getSettings().get("OutputModel").length() == 0) {
			modelException();
		} else {
			model = params.getSettings().get("OutputModel");
		}
		return model;
	}

	public static String getCorpusFormat(TrainingParameters params) {
		String corpusFormat = null;
		if (params.getSettings().get("CorpusFormat") == null) {
			corpusFormatException();
		} else {
			corpusFormat = params.getSettings().get("CorpusFormat");
		}
		return corpusFormat;
	}

	public static Integer getBeamsize(TrainingParameters params) {
		Integer beamsize = null;
		if (params.getSettings().get("BeamSize") == null) {
			beamsize = Flags.DEFAULT_BEAM_SIZE;
		} else {
			beamsize = Integer.parseInt(params.getSettings().get("BeamSize"));
		}
		return beamsize;
	}
	
	public static String getFeatureSet(TrainingParameters params) {
		String featureSet = null;
		if (params.getSettings().get("FeatureSet") != null) {
			featureSet = params.getSettings().get("FeatureSet");
		} else {
			featureSet = Flags.DEFAULT_FEATURESET_FLAG;
		}
		return featureSet;
	}

	public static String getDictionaryFeatures(TrainingParameters params) {
		String dictionaryFlag = null;
		if (params.getSettings().get("DictionaryFeatures") != null) {
			dictionaryFlag = params.getSettings().get("DictionaryFeatures");
		} else {
			dictionaryFlag = Flags.DEFAULT_FEATURE_FLAG;
		}
		return dictionaryFlag;
	}

	public static Integer getAutoDictFeatures(TrainingParameters params) {
		String dictionaryFlag = null;
		if (params.getSettings().get("AutoDictFeatures") != null) {
			dictionaryFlag = params.getSettings().get("AutoDictFeatures");
		} else {
			dictionaryFlag = Flags.DEFAULT_FEATURE_FLAG;
		}
		return Integer.parseInt(dictionaryFlag);
	}

	public static Integer getFolds(TrainingParameters params) {
		Integer folds = null;
		if (params.getSettings().get("Folds") == null) {
			folds = Flags.DEFAULT_FOLDS_VALUE;
		} else {
			folds = Integer.parseInt(params.getSettings().get("Folds"));
		}
		return folds;
	}

	public static void modelException() {
		System.err
				.println("Please provide a model in the OutputModel field in the parameters file!");
		System.exit(1);
	}

	public static void langException() {
		System.err
				.println("Please fill in the Language field in the parameters file!");
		System.exit(1);
	}

	public static void datasetException() {
		System.err
				.println("Please specify your training/testing sets in the TrainSet and TestSet fields in the parameters file!");
		System.exit(1);
	}

	public static void corpusFormatException() {
		System.err
				.println("Please fill in CorpusFormat field in the parameters file!");
		System.exit(1);
	}

	public static void dictionaryException() {
		System.err
				.println("You need to set the --dictPath option to the dictionaries directory to use the dictTag option!");
		System.exit(1);
	}

	public static void dictionaryFeaturesException() {
		System.err
				.println("You need to specify the DictionaryFeatures in the parameters file to use the DictionaryPath!");
		System.exit(1);
	}

	public static boolean isDictionaryFeatures(TrainingParameters params) {
		String dictFeatures = getDictionaryFeatures(params);
		return !dictFeatures.equalsIgnoreCase(Flags.DEFAULT_FEATURE_FLAG);
	}

}
