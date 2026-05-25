package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

import java.util.List;

import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionFeatures;

import fuzzlib.DefuzMethod;
import fuzzlib.FuzzySet;
import fuzzlib.norms.SNorm;
import fuzzlib.norms.TNorm;
import fuzzlib.reasoning.ReasoningSystem;
import fuzzlib.reasoning.SystemConfig;

final class FraudReasoningSystemFactory {
	private static final String OUTPUT_VARIABLE = "fraudProbability";
	private static final double CONCLUSION_WIDTH = 0.15;
	private static final double MIN_RULE_PEAK = 0.2;

	private final List<FuzzyFeatureSet> featureSets;
	private final List<FuzzyRule> rules;
	private final double maxRuleStrength;
	private final ThreadLocal<ReasoningSystem> reasoningSystem;

	FraudReasoningSystemFactory(List<FuzzyFeatureSet> featureSets, List<FuzzyRule> rules) {
		this.featureSets = featureSets;
		this.rules = rules;
		this.maxRuleStrength = rules.stream()
				.mapToDouble(FraudReasoningSystemFactory::ruleStrength)
				.max()
				.orElse(1.0);
		this.reasoningSystem = ThreadLocal.withInitial(this::createSystem);
	}

	double infer(EthTransactionFeatures features) {
		ReasoningSystem system = reasoningSystem.get();
		system.setInput(features.toArray());
		system.Process();
		return clamp(system.getOutput(0));
	}

	private ReasoningSystem createSystem() {
		SystemConfig config = new SystemConfig(SystemConfig.DEF_MAMDANI);
		config.setInputWidth(featureSets.size());
		config.setOutputWidth(1);
		config.setNumberOfPremiseSets(featureSets.size() * FuzzyTerm.values().length);
		config.setNumberOfConclusionSets(rules.size());
		config.setIsOperationType(TNorm.TN_MINIMUM);
		config.setAndOperationType(TNorm.TN_PRODUCT);
		config.setOrOperationType(SNorm.SN_MAXIMUM);
		config.setImplicationType(TNorm.TN_MINIMUM);
		config.setConclusionAgregationType(SNorm.SN_MAXIMUM);
		config.setDefuzzyfication(DefuzMethod.DF_COG);

		ReasoningSystem system = new ReasoningSystem(config);
		describeVariables(system);
		addPremiseSets(system);
		addRules(system);
		return system;
	}

	private void describeVariables(ReasoningSystem system) {
		for (int i = 0; i < featureSets.size(); i++) {
			FuzzyFeatureSet featureSet = featureSets.get(i);
			system.describeInputVar(i, featureSet.featureName(), featureSet.featureName());
		}
		system.describeOutputVar(0, OUTPUT_VARIABLE, "Fraud probability");
	}

	private void addPremiseSets(ReasoningSystem system) {
		for (FuzzyFeatureSet featureSet : featureSets) {
			for (FuzzyTerm term : FuzzyTerm.values()) {
				system.addPremiseSet(featureSet.set(term));
			}
		}
	}

	private void addRules(ReasoningSystem system) {
		for (int i = 0; i < rules.size(); i++) {
			FuzzyRule rule = rules.get(i);
			String conclusionSetId = "fraud_probability_rule_" + i;
			system.addConclusionSet(conclusionSet(conclusionSetId, rule.consequentFraudProbability(), rulePeak(rule)));
			system.addRule();
			try {
				system.addRuleItem(rule.featureName(), featureSets.get(rule.featureIndex()).setId(rule.term()));
				system.addRuleConclusion(OUTPUT_VARIABLE, conclusionSetId);
			} catch (Exception exception) {
				throw new IllegalStateException("Could not configure fuzzy rule for " + rule.featureName(), exception);
			}
		}
	}

	private FuzzySet conclusionSet(String id, double center, double peak) {
		FuzzySet set = new FuzzySet(id, "Fraud probability conclusion");
		double left = Math.max(0.0, center - CONCLUSION_WIDTH);
		double right = Math.min(1.0, center + CONCLUSION_WIDTH);
		set.addPoint(left, left == center ? peak : 0.0);
		set.addPoint(center, peak);
		set.addPoint(right, right == center ? peak : 0.0);
		return set;
	}

	private double rulePeak(FuzzyRule rule) {
		if (maxRuleStrength == 0.0) {
			return 1.0;
		}
		double normalized = ruleStrength(rule) / maxRuleStrength;
		return MIN_RULE_PEAK + (1.0 - MIN_RULE_PEAK) * normalized;
	}

	private static double ruleStrength(FuzzyRule rule) {
		return rule.support() * Math.abs(rule.confidenceLift());
	}

	private static double clamp(double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.clamp(value, 0.0, 1.0);
	}
}
