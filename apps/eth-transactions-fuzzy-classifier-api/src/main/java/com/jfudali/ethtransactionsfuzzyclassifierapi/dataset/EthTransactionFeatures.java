package com.jfudali.ethtransactionsfuzzyclassifierapi.dataset;

public record EthTransactionFeatures(
		double blockNumber,
		double confirmations,
		double month,
		double day,
		double hour,
		double meanValueReceived,
		double varianceValueReceived,
		double totalReceived,
		double timeDiffFirstLastReceived,
		double totalTxSent,
		double totalTxSentUnique) {

	public static final String[] DATASET_FEATURE_NAMES = {
			"blockNumber",
			"confirmations",
			"Month",
			"Day",
			"Hour",
			"mean_value_received",
			"variance_value_received",
			"total_received",
			"time_diff_first_last_received",
			"total_tx_sent",
			"total_tx_sent_unique"
	};

	public static final String[] CLASSIFICATION_FEATURE_NAMES = {
			"confirmations",
			"mean_value_received",
			"variance_value_received",
			"total_received",
			"time_diff_first_last_received",
			"total_tx_sent",
			"total_tx_sent_unique"
	};

	public double[] toArray() {
		return new double[] {
				confirmations,
				meanValueReceived,
				varianceValueReceived,
				totalReceived,
				timeDiffFirstLastReceived,
				totalTxSent,
				totalTxSentUnique
		};
	}

	public static EthTransactionFeatures fromDatasetColumns(double[] row) {
		if (row.length < DATASET_FEATURE_NAMES.length) {
			throw new IllegalArgumentException("Expected at least " + DATASET_FEATURE_NAMES.length + " feature columns");
		}
		return new EthTransactionFeatures(
				row[0],
				row[1],
				row[2],
				row[3],
				row[4],
				row[5],
				row[6],
				row[7],
				row[8],
				row[9],
				row[10]);
	}
}
