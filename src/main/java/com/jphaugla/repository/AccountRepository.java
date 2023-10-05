package com.jphaugla.repository;
import com.jphaugla.domain.Account;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.jphaugla.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class AccountRepository {

	final Logger logger = LoggerFactory.getLogger(AccountRepository.class);
	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	@Qualifier("redisTemplateW1")
	private RedisTemplate<Object, Object> redisTemplateW1;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Value("${app.accountSearchIndexName}")
	private String accountSearchIndexName;

	public AccountRepository() {

		logger.info("AccountRepository constructor");
	}

	public String create(Account account) {
		logger.info("AccountRepository create with index=" + accountSearchIndexName);
		logger.info(" account " + account.toString());
		if (account.getCreatedDatetime() == null) {
			long currentTimeMillis = System.currentTimeMillis();
			String stringMillis = Long.toString(currentTimeMillis);
			account.setCreatedDatetime(stringMillis);
			account.setOpenDatetime(stringMillis);
			account.setLastUpdated(stringMillis);
		}

		Map<Object, Object> AccountHash = mapper.convertValue(account, Map.class);
		// while (AccountHash.values().remove(null));
		// logger.info( "before null removal with AccountHash " + AccountHash.toString());
		AccountHash.values().removeIf(Objects::isNull);

        // logger.info( "before putall with AccountHash " + AccountHash.toString());
		stringRedisTemplate.opsForHash().putAll(accountSearchIndexName + ':' + account.getAccountNo(), AccountHash);
		// redisTemplate.opsForHash().putAll("Account:" + Account.getAccountId(), AccountHash);
		// logger.info(String.format("Account with ID %s saved", account.getAccountNo()));
		return "Success\n";
	}

	public Account get(String accountId) {
		logger.info("in AccountRepository.get with Account id=" + accountId);
		String fullKey = accountSearchIndexName + ':' + accountId;
		Map<Object, Object> AccountHash = stringRedisTemplate.opsForHash().entries(fullKey);
		Account account = mapper.convertValue(AccountHash, Account.class);
		return (account);
	}


	public void createAll(List<Account> accounts) {
		for (Account account : accounts) {
			create(account);
		}
	}
}