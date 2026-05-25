package com.jfudali.ethtransactionsfuzzyclassifierapi.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfudali.ethtransactionsfuzzyclassifierapi.dataset.EthTransactionFeatures;
import com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy.FuzzyClassificationResult;
import com.jfudali.ethtransactionsfuzzyclassifierapi.fuzzy.FuzzyEthTransactionClassifier;

@RestController
@RequestMapping("/api/fuzzy-classifier")
public class FuzzyClassifierController {
	private final FuzzyEthTransactionClassifier classifier;

	public FuzzyClassifierController(FuzzyEthTransactionClassifier classifier) {
		this.classifier = classifier;
	}

	@PostMapping("/classify")
	public FuzzyClassificationResult classify(@RequestBody EthTransactionFeatures features) {
		return classifier.classify(features);
	}
}
