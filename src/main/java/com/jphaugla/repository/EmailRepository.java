package com.jphaugla.repository;

import com.jphaugla.domain.Email;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Repository;
@Repository

public class EmailRepository{
	private static final String KEY = "Email";


	final Logger logger = LoggerFactory.getLogger(com.jphaugla.repository.EmailRepository.class);
	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	@Qualifier("redisTemplateW1")
	private RedisTemplate<Object, Object> redisTemplateW1;

	@Autowired
	@Qualifier("redisTemplateR1")
	private RedisTemplate<Object, Object>  redisTemplateR1;

	public EmailRepository() {

		logger.info("EmailRepository constructor");
	}

	public String create(Email email) {

		Map<Object, Object> emailHash = mapper.convertValue(email, Map.class);
		redisTemplateW1.opsForHash().putAll("Email:" + email.getEmailAddress(), emailHash);
		// redisTemplate.opsForHash().putAll("Email:" + email.getEmailId(), emailHash);
		logger.info(String.format("Email with ID %s saved", email.getEmailAddress()));
		return "Success\n";
	}

	public Email get(String emailId) {
		logger.info("in EmailRepository.get with email id=" + emailId);
		String fullKey = "Email:" + emailId;
		Map<Object, Object> emailHash = redisTemplateR1.opsForHash().entries(fullKey);
		Email email = mapper.convertValue(emailHash, Email.class);
		return (email);
	}


}
