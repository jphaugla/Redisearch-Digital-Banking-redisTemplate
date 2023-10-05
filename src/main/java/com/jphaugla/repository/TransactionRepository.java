package com.jphaugla.repository;

import com.jphaugla.domain.Transaction;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
@Repository

public class TransactionRepository{
	private static final String KEY = "Transaction";
	@Value("${app.transactionSearchIndexName}")
	private String transactionSearchIndexName;

	final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);
	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	@Qualifier("redisTemplateW1")
	private RedisTemplate<Object, Object> redisTemplateW1;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	public TransactionRepository() {

		logger.info("TransactionRepository constructor");
	}

	public String create(Transaction transaction) {
		logger.info("entering TransactionReposistory create transaction " + transaction.toString());
		if (transaction.getInitialDate() == null) {
			long currentTimeMillis = System.currentTimeMillis();
			transaction.setInitialDate(Long.toString(currentTimeMillis));
		}

		Map<Object, Object> transactionHash = mapper.convertValue(transaction, Map.class);
		while (transactionHash.values().remove(null));
		stringRedisTemplate.opsForHash().putAll(transactionSearchIndexName + ':' + transaction.getTranId(), transactionHash);
		// redisTemplate.opsForHash().putAll("Transaction:" + transaction.getTransactionId(), transactionHash);
		// logger.info(String.format("Transaction with ID %s saved", transaction.getTranId()));
		return "Success\n";
	}

	public String createAll(List<Transaction> transactionList) {
		for (Transaction transaction : transactionList) {
			create(transaction);
		}
		return "Success\n";
	}

	public Transaction get(String transactionId) {
		logger.info("in TransactionRepository.get with transaction id=" + transactionId);
		String fullKey = "Transaction:" + transactionId;
		Map<Object, Object> transactionHash = stringRedisTemplate.opsForHash().entries(fullKey);
		Transaction transaction = mapper.convertValue(transactionHash, Transaction.class);
		return (transaction);
	}


}
