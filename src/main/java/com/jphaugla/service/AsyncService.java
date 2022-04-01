package com.jphaugla.service;

import com.jphaugla.domain.*;
import com.jphaugla.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;



@Service
public class AsyncService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    @Qualifier("redisTemplateW1")
    private RedisTemplate<Object, Object> redisTemplateW1;

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeAllTransaction(List<Transaction> transactions) {
        transactionRepository.createAll(transactions);
        return CompletableFuture.completedFuture(0);
    }
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeTransaction(Transaction transaction) {
        transactionRepository.create(transaction);
        return CompletableFuture.completedFuture(0);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeAllAccounts(List<Account> accounts){
        // Integer count = accounts.size();
        accountRepository.createAll(accounts);
        return CompletableFuture.completedFuture(0);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeAccounts(Account account){
        // Integer count = accounts.size();
        accountRepository.create(account);
        return CompletableFuture.completedFuture(0);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeCustomer(Customer customer) {
        customerRepository.create(customer);
        return CompletableFuture.completedFuture(0);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writePhone(Phone phoneNumber) {
        phoneRepository.create(phoneNumber);
        return CompletableFuture.completedFuture(0);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> writeEmail(Email email) {
        emailRepository.create(email);
        return CompletableFuture.completedFuture(0);
    }




}
