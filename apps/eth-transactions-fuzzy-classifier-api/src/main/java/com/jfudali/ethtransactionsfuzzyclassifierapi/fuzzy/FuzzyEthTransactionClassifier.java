package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionDatasetLoader;
import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionFeatures;
import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionSample;

@Service
public class FuzzyEthTransactionClassifier {
	private static final double DECISION_THRESHOLD = 0.5;


	private final FraudReasoningSystemFactory fraudReasoningSystemFactory;

	public FuzzyEthTransactionClassifier(EthTransactionDatasetLoader loader) {
		List<EthTransactionSample> trainingSamples = loader.load();
		if (trainingSamples.isEmpty()) {
			throw new IllegalStateException("ETFD dataset is empty");
		}

		FuzzyTrainingDataset trainingDataset = FuzzyTrainingDataset.from(trainingSamples);
		List<FuzzyFeatureSet> featureSets = FuzzyFeatureSet.fromFeatureColumns(
				EthTransactionFeatures.CLASSIFICATION_FEATURE_NAMES,
				trainingDataset.featureColumns());
		List<FuzzyRule> rules = FuzzyRuleLearner.learn(trainingDataset, featureSets);
		this.fraudReasoningSystemFactory = new FraudReasoningSystemFactory(featureSets, rules);
	}

  // TODO: Obliczyć accuracy, walidacja krzyżowa( przy słabych wynikach pomyśl co zrobić jeszcze na etapie preprocessingu)
	public FuzzyClassificationResult classify(EthTransactionFeatures features) {
		double fraudProbability = fraudReasoningSystemFactory.infer(features);

		return new FuzzyClassificationResult(
				fraudProbability >= DECISION_THRESHOLD,
				FuzzyMath.round(fraudProbability));
	}
}
