package com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy;


import lombok.NoArgsConstructor;

@NoArgsConstructor
final class FuzzyMath {

	static double quantile(double[] sortedValues, double quantile) {
		double position = quantile * (sortedValues.length - 1);
		int lower = (int) Math.floor(position);
		int upper = (int) Math.ceil(position);
		if (lower == upper) {
			return sortedValues[lower];
		}
		double fraction = position - lower;
		return sortedValues[lower] + (sortedValues[upper] - sortedValues[lower]) * fraction;
	}

	static double ratio(double numerator, double denominator) {
		return denominator == 0.0 ? 0.0 : numerator / denominator;
	}

	static double round(double value) {
		return Math.round(value * 10000.0) / 10000.0;
	}
}
