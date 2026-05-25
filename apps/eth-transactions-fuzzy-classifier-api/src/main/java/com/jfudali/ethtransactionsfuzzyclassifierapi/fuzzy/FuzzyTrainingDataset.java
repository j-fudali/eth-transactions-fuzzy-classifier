package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

import java.util.List;

import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionFeatures;
import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionSample;

record FuzzyTrainingDataset(
		List<EthTransactionSample> samples,
		double[][] featureColumns,
		double fraudPrior) {

	static FuzzyTrainingDataset from(List<EthTransactionSample> samples) {
		return new FuzzyTrainingDataset(
				List.copyOf(samples),
				buildFeatureColumns(samples),
				fraudPrior(samples));
	}

	int size() {
		return samples.size();
	}

	double featureValue(int featureIndex, int sampleIndex) {
		return featureColumns[featureIndex][sampleIndex];
	}

	boolean fraud(int sampleIndex) {
		return samples.get(sampleIndex).fraud();
	}

	private static double[][] buildFeatureColumns(List<EthTransactionSample> samples) {
		int featureCount = EthTransactionFeatures.CLASSIFICATION_FEATURE_NAMES.length;
		double[][] columns = new double[featureCount][samples.size()];
		for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++) {
			copyFeaturesToColumns(samples.get(sampleIndex).features(), columns, sampleIndex);
		}
		return columns;
	}

	private static void copyFeaturesToColumns(EthTransactionFeatures features, double[][] columns, int sampleIndex) {
		double[] values = features.toArray();
		for (int featureIndex = 0; featureIndex < values.length; featureIndex++) {
			columns[featureIndex][sampleIndex] = values[featureIndex];
		}
	}

	private static double fraudPrior(List<EthTransactionSample> samples) {
		return samples.stream().filter(EthTransactionSample::fraud).count() / (double) samples.size();
	}
}
