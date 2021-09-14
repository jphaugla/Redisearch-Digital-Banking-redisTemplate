package com.jphaugla.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jphaugla.data.BankGenerator;
import com.jphaugla.domain.*;
import com.jphaugla.repository.*;

import com.redislabs.mesclun.search.*;
import com.redislabs.mesclun.StatefulRedisModulesConnection;
import com.redislabs.mesclun.search.aggregate.GroupBy;
import com.redislabs.mesclun.search.aggregate.reducers.Count;

import io.lettuce.core.RedisCommandExecutionException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;


@Service

public class BankService {

	private static BankService bankService = new BankService();
	@Autowired
	private AsyncService asyncService;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PhoneRepository phoneRepository;
	@Autowired
	private EmailRepository emailRepository;
	@Autowired
	private MerchantRepository merchantRepository;
	@Autowired
	private TransactionReturnRepository transactionReturnRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private StringRedisTemplate redisTemplate;
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private StatefulRedisModulesConnection<String,String> connection;

	@Value("${app.transactionSearchIndexName}")
	private String transactionSearchIndexName;
	@Value("${app.customerSearchIndexName}")
	private String customerSearchIndexName;
	@Value("${app.merchantSearchIndexName}")
	private String merchantSearchIndexName;
	@Value("${app.accountSearchIndexName}")
	private String accountSearchIndexName;
	private static final Logger logger = LoggerFactory.getLogger(BankService.class);

	private long timerSum = 0;
	private AtomicLong timerCount= new AtomicLong();
	
	public static BankService getInstance(){
		return bankService;		
	}

	public Optional<Customer> getCustomer(String customerId){
		logger.info("in bankservice.getCustomer with ID " + customerId);
		Customer returnCustomer = customerRepository.get(customerId);
		logger.info("returned customer " + returnCustomer);
		return Optional.of(returnCustomer);
	}
	public void saveSampleCustomer() throws ParseException, RedisCommandExecutionException {
		Date create_date = new SimpleDateFormat("yyyy.MM.dd").parse("2020.03.28");
		Date last_update = new SimpleDateFormat("yyyy.MM.dd").parse("2020.03.29");
		String cust = "cust0001";
		Email home_email = new Email("jasonhaugland@gmail.com", "home", cust);
		Email work_email = new Email("jason.haugland@redislabs.com", "work", cust);
		PhoneNumber cell_phone = new PhoneNumber("612-408-4394", "cell", cust);
                /* emailRepository.save(home_email);
                emailRepository.save(work_email);
                phoneRepository.save(cell_phone);
                 */
		Customer customer = new Customer( cust, "4744 17th av s", "",
				"Home", "N", "Minneapolis", "00",
				"jph", create_date.getTime(), "IDR",
				"A", "BANK", "1949.01.23",
				"Ralph", "Ralph Waldo Emerson", "M",
				"887778989", "SSN", "Emerson", last_update.getTime(),
				"jph", "Waldo",  "MR",
				"help", "MN", "55444", "55444-3322"
		);
		customerRepository.create(customer);
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void postCustomer(Customer customer) {
		logger.info("in postCustomer with Customer =" + customer);
		customerRepository.create(customer);
	}

    public int deleteCustomer(String customerString) {
			// List<Customer> customerIDList = customerRepository.findByStateAbbreviationAndCity(state, city);
			RediSearchCommands<String, String> commands = connection.sync();
			String queryString = "@customerId:" + customerString;
			logger.info("query string is " + queryString);
			int returnValue = 0;
			SearchResults<String, String> results = commands.search(customerSearchIndexName, queryString);
			returnValue = results.size();
			for (Document document : results) {
				String fullKey = (String) document.getId();
				redisTemplate.delete(fullKey);
				// logger.warn("adding to transaction list string=" + onlyID + " fullKey is " + fullKey)
			}
			return (returnValue);
	}
}
