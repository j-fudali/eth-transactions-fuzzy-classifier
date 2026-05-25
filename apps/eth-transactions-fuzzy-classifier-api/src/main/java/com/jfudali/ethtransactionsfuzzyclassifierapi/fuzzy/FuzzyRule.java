package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

record FuzzyRule(
		int featureIndex,
		String featureName,
		FuzzyTerm term,
		double consequentFraudProbability,
		double support,
		double confidenceLift) {
}
