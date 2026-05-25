package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
final class FuzzyRuleLearner {
	private static final double MIN_RULE_SUPPORT = 0.01;
	private static final double MIN_CONFIDENCE_LIFT = 0.08;
	private static final int MAX_RULES = 24;

	static List<FuzzyRule> learn(FuzzyTrainingDataset dataset, List<FuzzyFeatureSet> featureSets) {
		List<FuzzyRule> candidates = new ArrayList<>();
		for (int featureIndex = 0; featureIndex < featureSets.size(); featureIndex++) {
			addRulesForFeature(candidates, dataset, featureSets.get(featureIndex), featureIndex);
		}
		return candidates.stream()
				.sorted(Comparator.comparingDouble(FuzzyRuleLearner::ruleStrength).reversed())
				.limit(MAX_RULES)
				.toList();
	}

	private static void addRulesForFeature(
			List<FuzzyRule> candidates,
			FuzzyTrainingDataset dataset,
			FuzzyFeatureSet featureSet,
			int featureIndex) {
		for (FuzzyTerm term : FuzzyTerm.values()) {
			candidateRule(dataset, featureSet, featureIndex, term).ifPresent(candidates::add);
		}
	}

	private static Optional<FuzzyRule> candidateRule(
			FuzzyTrainingDataset dataset,
			FuzzyFeatureSet featureSet,
			int featureIndex,
			FuzzyTerm term) {
		RuleActivation activation = activation(dataset, featureSet, featureIndex, term);
		if (activation.activationSum() == 0.0) {
			return Optional.empty();
		}

		double support = activation.activationSum() / dataset.size();
		double consequentFraudProbability = activation.fraudActivationSum() / activation.activationSum();
		double confidenceLift = consequentFraudProbability - dataset.fraudPrior();
		if (support < MIN_RULE_SUPPORT || Math.abs(confidenceLift) < MIN_CONFIDENCE_LIFT) {
			return Optional.empty();
		}

		return Optional.of(new FuzzyRule(
				featureIndex,
				featureSet.featureName(),
				term,
				consequentFraudProbability,
				support,
				confidenceLift));
	}

	private static RuleActivation activation(
			FuzzyTrainingDataset dataset,
			FuzzyFeatureSet featureSet,
			int featureIndex,
			FuzzyTerm term) {
		double activationSum = 0.0;
		double fraudActivationSum = 0.0;
		for (int sampleIndex = 0; sampleIndex < dataset.size(); sampleIndex++) {
			double membership = featureSet.membership(term, dataset.featureValue(featureIndex, sampleIndex));
			activationSum += membership;
			if (dataset.fraud(sampleIndex)) {
				fraudActivationSum += membership;
			}
		}
		return new RuleActivation(activationSum, fraudActivationSum);
	}

	private static double ruleStrength(FuzzyRule rule) {
		return Math.abs(rule.confidenceLift()) * rule.support();
	}

	private record RuleActivation(double activationSum, double fraudActivationSum) {
	}
}
