package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import fuzzlib.FuzzySet;

final class FuzzyFeatureSet {
	private final String featureName;
	private final Map<FuzzyTerm, FuzzySet> sets;

	static List<FuzzyFeatureSet> fromFeatureColumns(String[] featureNames, double[][] featureColumns) {
		if (featureNames.length != featureColumns.length) {
			throw new IllegalArgumentException("Feature names count must match feature column count");
		}
		List<FuzzyFeatureSet> sets = new ArrayList<>();
		for (int featureIndex = 0; featureIndex < featureNames.length; featureIndex++) {
			double[] values = Arrays.copyOf(featureColumns[featureIndex], featureColumns[featureIndex].length);
			Arrays.sort(values);
			sets.add(fromSortedValues(featureNames[featureIndex], values));
		}
		return List.copyOf(sets);
	}

	private FuzzyFeatureSet(String featureName, double min, double q25, double q50, double q75, double max) {
		this.featureName = featureName;
		this.sets = new EnumMap<>(FuzzyTerm.class);

		double spread = Math.max(max - min, 1.0);
		double epsilon = spread * 0.000001;
		if (q25 <= min) {
			q25 = min + epsilon;
		}
		if (q50 <= q25) {
			q50 = q25 + epsilon;
		}
		if (q75 <= q50) {
			q75 = q50 + epsilon;
		}
		if (max <= q75) {
			max = q75 + epsilon;
		}

		sets.put(FuzzyTerm.LOW, lowSet(featureName + "_LOW", min, q25, q50));
		sets.put(FuzzyTerm.MEDIUM, triangle(featureName + "_MEDIUM", q50, q50 - q25, q75 - q50));
		sets.put(FuzzyTerm.HIGH, highSet(featureName + "_HIGH", q50, q75, max));
	}

	String featureName() {
		return featureName;
	}

	double membership(FuzzyTerm term, double value) {
		return sets.get(term).getMembership(value);
	}

	FuzzySet set(FuzzyTerm term) {
		return sets.get(term);
	}

	String setId(FuzzyTerm term) {
		return sets.get(term).getId();
	}

	static FuzzyFeatureSet fromSortedValues(String featureName, double[] sortedValues) {
		if (sortedValues.length == 0) {
			throw new IllegalArgumentException("Cannot build fuzzy set for empty feature: " + featureName);
		}
		return new FuzzyFeatureSet(
				featureName,
				sortedValues[0],
				FuzzyMath.quantile(sortedValues, 0.25),
				FuzzyMath.quantile(sortedValues, 0.50),
				FuzzyMath.quantile(sortedValues, 0.75),
				sortedValues[sortedValues.length - 1]);
	}

	private static FuzzySet lowSet(String id, double min, double shoulderEnd, double zeroAt) {
		FuzzySet set = new FuzzySet(id, "Low values");
		set.addPoint(min, 1.0);
		set.addPoint(shoulderEnd, 1.0);
		set.addPoint(zeroAt, 0.0);
		return set;
	}

	private static FuzzySet highSet(String id, double zeroAt, double shoulderStart, double max) {
		FuzzySet set = new FuzzySet(id, "High values");
		set.addPoint(zeroAt, 0.0);
		set.addPoint(shoulderStart, 1.0);
		set.addPoint(max, 1.0);
		return set;
	}

	private static FuzzySet triangle(String id, double center, double leftWidth, double rightWidth) {
		FuzzySet set = new FuzzySet(id, "Medium values");
		set.newTriangle(center, leftWidth, rightWidth);
		return set;
	}
}
