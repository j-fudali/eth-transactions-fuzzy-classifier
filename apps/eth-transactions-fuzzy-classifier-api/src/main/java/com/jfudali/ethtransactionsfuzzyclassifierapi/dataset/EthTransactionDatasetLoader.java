package com.jfudali.ethtransactionsfuzzyclassifierapi.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EthTransactionDatasetLoader {
	private static final String DATASET_PATH = "datasets/ETFD_Dataset.txt";
	private static final String FRAUD_COLUMN = "Fraud";

	public List<EthTransactionSample> load() {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DATASET_PATH);
		if (stream == null) {
			throw new IllegalStateException("Dataset not found on classpath: " + DATASET_PATH);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null) {
				return List.of();
			}
			Map<String, Integer> columnIndexes = columnIndexes(header);
			int fraudIndex = requiredIndex(columnIndexes, FRAUD_COLUMN);
			int[] featureIndexes = featureIndexes(columnIndexes);

			List<EthTransactionSample> samples = new ArrayList<>();
			int lineNumber = 1;
			String line;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) {
					continue;
				}
				samples.add(parseLine(line, lineNumber, featureIndexes, fraudIndex));
			}
			return List.copyOf(samples);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not load dataset: " + DATASET_PATH, exception);
		}
	}

	private EthTransactionSample parseLine(String line, int lineNumber, int[] featureIndexes, int fraudIndex) {
		String[] columns = line.strip().split("\\t", -1);
		requireColumn(columns, fraudIndex, FRAUD_COLUMN, lineNumber);

		double[] values = new double[EthTransactionFeatures.DATASET_FEATURE_NAMES.length];
		for (int i = 0; i < values.length; i++) {
			String featureName = EthTransactionFeatures.DATASET_FEATURE_NAMES[i];
			int columnIndex = featureIndexes[i];
			requireColumn(columns, columnIndex, featureName, lineNumber);
			values[i] = Double.parseDouble(columns[columnIndex]);
		}
		boolean fraud = Integer.parseInt(columns[fraudIndex].trim()) == 1;
		return new EthTransactionSample(EthTransactionFeatures.fromDatasetColumns(values), fraud);
	}

	private static Map<String, Integer> columnIndexes(String header) {
		String[] columns = header.strip().split("\\t", -1);
		Map<String, Integer> indexes = new HashMap<>();
		for (int i = 0; i < columns.length; i++) {
			indexes.put(columns[i].trim(), i);
		}
		return indexes;
	}

	private static int[] featureIndexes(Map<String, Integer> columnIndexes) {
		int[] indexes = new int[EthTransactionFeatures.DATASET_FEATURE_NAMES.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = requiredIndex(columnIndexes, EthTransactionFeatures.DATASET_FEATURE_NAMES[i]);
		}
		return indexes;
	}

	private static int requiredIndex(Map<String, Integer> columnIndexes, String columnName) {
		Integer index = columnIndexes.get(columnName);
		if (index == null) {
			throw new IllegalArgumentException("Dataset header does not contain column: " + columnName);
		}
		return index;
	}

	private static void requireColumn(String[] columns, int columnIndex, String columnName, int lineNumber) {
		if (columns.length <= columnIndex || columns[columnIndex].isBlank()) {
			throw new IllegalArgumentException(
					"Invalid ETFD row at line " + lineNumber + ": expected column " + columnName + " at index " + columnIndex);
		}
	}
}
