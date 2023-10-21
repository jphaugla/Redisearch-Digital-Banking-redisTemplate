package com.jphaugla.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jphaugla.data.BankGenerator;
import com.jphaugla.domain.*;
import com.jphaugla.repository.*;

import io.lettuce.core.RedisCommandExecutionException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.env.Environment;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Reducers;


@Service

public class BankService {

	private static BankService bankService = new BankService();
	@Autowired
	private AsyncService asyncService;

	@Autowired
	private TopicProducer topicProducer;
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


	@Value("${app.transactionSearchIndexName}")
	private String transactionSearchIndexName;
	@Value("${app.transactionReturnSearchIndexName}")
	private String transactionReturnSearchIndexName;
	@Value("${app.customerSearchIndexName}")
	private String customerSearchIndexName;
	@Value("${app.merchantSearchIndexName}")
	private String merchantSearchIndexName;
	@Value("${app.accountSearchIndexName}")
	private String accountSearchIndexName;
	private static final Logger logger = LoggerFactory.getLogger(BankService.class);

	private long timerSum = 0;
	private AtomicLong timerCount= new AtomicLong();
	@Autowired
	private Environment env;

	UnifiedJedis client;

	public static BankService getInstance(){
		return bankService;		
	}
	//
	//  Customer
	//
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
		Phone cell_phone = new Phone("612-408-4394", "cell", cust);
		emailRepository.create(home_email);
		emailRepository.create(work_email);
		phoneRepository.create(cell_phone);
		Customer customer = new Customer( cust, "4744 17th av s", "",
				"Home", "N", "Minneapolis", "00",
				"jph", Long.toString(create_date.getTime()), "IDR",
				"A", "BANK", "1949.01.23",
				"Ralph", "Ralph Waldo Emerson", "M",
				"887778989", "SSN", "Emerson", Long.toString(last_update.getTime()),
				"jph", "Waldo",  "MR",
				"help", "MN", "55444", "55444-3322"
		);
		customerRepository.create(customer);
	}

	public void postCustomer(Customer customer) {
		logger.info("in postCustomer with Customer =" + customer);
		customerRepository.create(customer);
		Email home_email = new Email(customer.getCustomerId() + "@gmail.com", "home", customer.getCustomerId());
		Email work_email = new Email(customer.getCustomerId() + "@redislabs.com", "work", customer.getCustomerId());
		Phone cell_phone = new Phone("612-408-4394", "cell", customer.getCustomerId());
		emailRepository.create(home_email);
		emailRepository.create(work_email);
		phoneRepository.create(cell_phone);
	}

	public SearchResult search(String indexName, String queryString, int offset, int limit, String sortBy, boolean ascending) {
		// Let's put all the informations in a Map top make it easier to return JSON object
		// no need to have "predefine mapping"
		client = jedis_connection();
		Map<String, Object> returnValue = new HashMap<>();
		Map<String, Object> resultMeta = new HashMap<>();
		logger.info("starting search with querystring" + queryString);
		// Create a simple query
		Query query = new Query(queryString)
				.setWithScores()
				.limit(offset, limit);
		// if sort by parameter add it to the query
		if (sortBy != null && !sortBy.isEmpty()) {
			query.setSortBy(sortBy, ascending); // Ascending by default
		}

		// Execute the query

        return client.ftSearch(indexName, query);
	}


	public SearchResult search(String indexName, String queryString) {
		return search(indexName, queryString, 0, 10, null, true);
	}


	public int deleteCustomer(String customerString) {
		// List<Customer> customerIDList = customerRepository.findByStateAbbreviationAndCity(state, city);
		// client = jedis_connection();
		String queryString = buildTagQuery("customerId", customerString);
		logger.info("query string is " + queryString);
		int returnValue = 0;
		SearchResult results =search(customerSearchIndexName, queryString);
		List<Document> docs = results.getDocuments();
		for (Document document : docs) {
			String fullKey = (String) document.getId();
			redisTemplate.delete(fullKey);
			returnValue++;
			// logger.warn("adding to transaction list string=" + onlyID + " fullKey is " + fullKey)
		}
		return (returnValue);
	}

	public SearchResult getCustomerByStateCity(String state, String city){

		// List<Customer> customerIDList = customerRepository.findByStateAbbreviationAndCity(state, city);
		String stateQuery = buildTagQuery("stateAbbreviation",state);
		String cityQuery = buildTagQuery("city", city);
		String queryString = stateQuery + " " + cityQuery;
		logger.info("query string is " + queryString);
        return search(customerSearchIndexName, queryString);
	}

	public SearchResult getCustomerIdsbyZipcodeLastname(String zipcode, String lastName){
		String zipString = buildTagQuery("zipcode",zipcode);
		String lastNameStaring = buildTagQuery("lastName", lastName);
		String queryString = zipString + " " + lastNameStaring;
        return search(customerSearchIndexName, queryString);
	}

	//
	// Phone
	//
	public Optional<Phone> getPhoneNumber(String phoneString) {
		return phoneRepository.get(phoneString);
	}

	public Customer getCustomerByPhone(String phoneString) {
		// get list of customers having this phone number
		//  first, get phone hash with this phone number
		//   next, get the customer id with this phone number
		//   third, use the customer id to get the customer
		Optional<Phone> optPhone = getPhoneNumber(phoneString);
		Optional<Customer> returnCustomer = null;
		Customer returnCust = null;
		logger.info("in bankservice.getCustomerByPhone optphone is" + optPhone.isPresent());
		if (optPhone.isPresent()) {
			Phone onePhone = optPhone.get();
			String customerId = onePhone.getCustomerId();
			logger.info(" onePhone is " + onePhone.getPhoneNumber() + ":" + onePhone.getPhoneLabel() + ":" + onePhone.getCustomerId());
			returnCustomer = Optional.ofNullable(customerRepository.get(customerId));
		}

		if ((returnCustomer != null) && (returnCustomer.isPresent())) {
			returnCust = returnCustomer.get();
			// logger.info("customer is " + returnCust);

		}
		return returnCust;
	}
	//
	// Email
	//
	public Optional<Email> getEmail(String email) {
		return Optional.ofNullable(emailRepository.get(email));
	}

	public Customer getCustomerByEmail(String emailString) {
		// get list of customers having this email number
		//  first, get email hash with this email number
		//   next, get the customer id with this email number
		//   third, use the customer id to get the customer
		Optional<Email> optionalEmail = getEmail(emailString);
		Optional<Customer> returnCustomer = Optional.empty();
		Customer returnCust = null;
		logger.info("in bankservice.getCustomerByEmail optEmail is" + optionalEmail.isPresent());
		if (optionalEmail.isPresent()) {
			Email oneEmail = optionalEmail.get();
			String customerId = oneEmail.getCustomerId();
			// logger.info("customer is " + customerId);
			returnCustomer = Optional.ofNullable(customerRepository.get(customerId));
		}

		if ( (returnCustomer.isPresent())) {
			returnCust = returnCustomer.get();
			logger.info("customer is " + returnCust);

		}
		return returnCust;
	}

	public int deleteCustomerEmail(String customerID) {
		logger.info("in bankservice.deleteCustomerEmail with CustomerID " + customerID);
        return emailRepository.deleteCustomerEmails(customerID);
	}

	//
	// Utility methods
	//
	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getDateFullDayQueryString(String stringDate) throws ParseException {
		Date inDate = new SimpleDateFormat("MM/dd/yyyy").parse(stringDate);
		long inUnix = inDate.getTime();
		//  since the transaction ID is also in the query can take a larger reach around the date column
		long startUnix = inUnix - 86400*1000;
		long endUnix = inUnix + 86400*1000;
		return " @postingDate:[" + startUnix + " " + endUnix + "]";
	}

	public String getDateToFromQueryString(Date startDate, Date endDate) throws ParseException {
		// Date toDate = new SimpleDateFormat("MM/dd/yyyy").parse(to);
		// Date fromDate = new SimpleDateFormat("MM/dd/yyyy").parse(from);
		long startUnix = startDate.getTime();
		long endUnix = endDate.getTime();
		return " @postingDate:[" + startUnix + " " + endUnix + "]";
	}

	//
    //  Transaction
	//

	public Transaction getTransaction(String transactionID) {
		Optional<Transaction> optionalTransaction;
		Transaction returnTransaction = null;
		optionalTransaction = Optional.ofNullable(transactionRepository.get(transactionID));
		if(optionalTransaction.isPresent()) {
			returnTransaction = optionalTransaction.get();
		}
		return returnTransaction;
	}

	private List<String> getTransactionByStatus(String transactionStatus) throws ExecutionException, InterruptedException {
		String queryString = buildTagQuery("status", transactionStatus);
		SearchResult results = search(transactionSearchIndexName, queryString, 0, 10000, null, true);
		// this code snippet get converts results to List of Transaction IDs
		List<String> transIdList = new ArrayList<String>();
		List<Document> docs = results.getDocuments();

		for (Document document : docs) {
			String fullKey = (String) document.getId();
			String onlyID = fullKey.replace(transactionSearchIndexName + ':', "");
			transIdList.add(onlyID);
			// logger.warn("adding to transaction list string=" + onlyID + " fullKey is " + fullKey);
		}
		return transIdList;
	}
	public void transactionStatusChange(String targetStatus) throws IllegalAccessException, ExecutionException, InterruptedException {
		//  move target from authorized->settled->posted
		logger.info("transactionStatusChange targetStatus is " + targetStatus);
		CompletableFuture<Integer> transaction_cntr = null;
		List<String> transIdList = new ArrayList<String>();
		long unixTime = System.currentTimeMillis();
		String stringUnixTime=String.valueOf(unixTime);
		if(targetStatus.equals("POSTED")) {
			transIdList = getTransactionByStatus("SETTLED");
		} else {
			transIdList = getTransactionByStatus("AUTHORIZED");
		}
		logger.info("number of transactions  " + transIdList.size());

		for(String tranID: transIdList) {
			redisTemplate.opsForHash().put(transactionSearchIndexName + ':' + tranID,"status", targetStatus);
			if(targetStatus.equals("POSTED")) {
				redisTemplate.opsForHash().put(transactionSearchIndexName + ':' + tranID, "postingDate", stringUnixTime);
			} else {
				redisTemplate.opsForHash().put(transactionSearchIndexName + ':' + tranID, "settlementDate", stringUnixTime);
			}
		}
		logger.info("Finished updating " + transIdList.size());
	}


	//   writeTransaction using crud without future
	private void writeTransaction(Transaction transaction) {
		// logger.info("writing a transaction " + transaction);
		transactionRepository.create(transaction);
	}
	// writeTransaction using crud with Future
	private CompletableFuture<Integer> writeTransactionFuture(Transaction transaction) throws IllegalAccessException {

		CompletableFuture<Integer> transaction_cntr = null;
		transaction_cntr = asyncService.writeTransaction(transaction);
		//   writes a sorted set to be used as the posted date index

		return transaction_cntr;
	}
	public List<Map<String, Object>>  transactionStatusReport() {
		client = jedis_connection();
		// AggregateResults<String> aggregateResults = commands.ftAggregate((transactionSearchIndexName, "*",
		// AggregateOptions.builder().load("status").operation(MRangeOptions.GroupBy.properties("status").reducer(Reducers.Count.as("COUNT")).build()).build());
		AggregationBuilder aggregation = new AggregationBuilder()
				.groupBy("@status", Reducers.count().as("COUNT"));
		logger.info("aggregation is" + aggregation.toString());
		AggregationResult aggrResult = client.ftAggregate(transactionSearchIndexName, aggregation);
		int resultSize = aggrResult.getResults().size();
		logger.info("result size is " + resultSize);
		logger.info(aggrResult.getResults().toString());
		List<Map<String, Object>> docsToReturn = new ArrayList<>();

        // AggregateOptions<String, String> groupByOptions = AggregateOptions.<String, String>operation(Group.by("status").reducer(Reducers.Count.as("COUNT")).build()).build();
		// AggregateResults<String> aggregateResults = commands.ftAggregate(transactionSearchIndexName, "*", groupByOptions);
		return (aggrResult.getResults());
	}

	public void saveSampleTransaction() throws ParseException, RedisCommandExecutionException {
		Date settle_date = new SimpleDateFormat("yyyy.MM.dd").parse("2021.07.28");
		Date post_date = new SimpleDateFormat("yyyy.MM.dd").parse("2021.07.28");
		Date init_date = new SimpleDateFormat("yyyy.MM.dd").parse("2021.07.27");

		Merchant merchant = new Merchant("Cub Foods", "5411",
				"Grocery Stores", "MN", "US");
		logger.info("before save merchant");
		merchantRepository.create(merchant);

		Transaction transaction = new Transaction("1234", "acct01",
				"Debit", merchant.getName() + ":" + "acct01", "referenceKeyType",
				"referenceKeyValue", "323.23",  "323.22", "1631",
				"Test Transaction", Long.toString(init_date.getTime()), Long.toString(settle_date.getTime()),
				Long.toString(post_date.getTime()), "POSTED", null, "ATM665", "Outdoor");
		logger.info("before save transaction");
		writeTransaction(transaction);
	}

	public void addTag(String transactionID, String tag, String operation) {
		logger.info("in addTag with transID=" + transactionID + " and tag " + tag);
		// hold set of transactions for a tag on an account
		String transactionKey = transactionSearchIndexName + ":" + transactionID;
		HashSet<String> tagHash = getTransactionTagList(transactionID);

		if(operation.equals("ADD")) {
			tagHash.add(tag);
		}
		else {
			tagHash.remove(tag);
		}
		String tagDelimitedString = String.join(":", tagHash);
		redisTemplate.opsForHash().put(transactionKey,"transactionTags",tagDelimitedString);
	}

	public HashSet <String> getTransactionTagList(String transactionID) {
		logger.info("in getTransactionTagList with transactionID=" + transactionID);
		// hold set of transactions for a tag on an account
		String transactionKey = transactionSearchIndexName + ":" + transactionID;
		String existingTags = (String) redisTemplate.opsForHash().get(transactionKey,"transactionTags");
		HashSet <String> tagHash = new HashSet<String>();
		if (existingTags != null) {
			String[] tagArray = existingTags.split(":");
			List<String> tagList = Arrays.asList(tagArray);
			tagHash = new HashSet<String>(tagList);
		}
		return tagHash;
	}


	public  SearchResult getTaggedTransactions(String accountNo, String tag) {
		logger.info("in getTaggedTransactions with accountNo=" + accountNo + " and tag " + tag);
		String accountNoString = buildTagQuery("accountNo", accountNo);
		String tranTagString = buildTagQuery("transactionTags", tag);
		String queryString = accountNoString + " " + tranTagString;
		logger.info("query is " + queryString);

        return search(transactionSearchIndexName, queryString);
	}

	/*
	public String testPipeline(Integer noOfRecords) {
		BankGenerator.Timer pipelineTimer = new BankGenerator.Timer();
		this.redisTemplate.executePipelined(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				connection.openPipeline();
				String keyAndValue=null;
				for (int index=0;index<noOfRecords;index++) {
					keyAndValue = "Silly"+index;
					connection.set(keyAndValue.getBytes(), keyAndValue.getBytes());
				}
				connection.closePipeline();
				return null;
			}
		});
		pipelineTimer.end();
		logger.info("Finished writing " + noOfRecords + " created in " +
				pipelineTimer.getTimeTakenSeconds() + " seconds.");
		return "Done";
	}
	*/

	public void generateData(Integer noOfCustomers, Integer noOfTransactions, Integer noOfDays,
							 String key_suffix, Boolean doKafka)
			throws ParseException, ExecutionException, InterruptedException, IllegalAccessException, RedisCommandExecutionException, JsonProcessingException {

		List<Account> accounts = createCustomerAccount(noOfCustomers, key_suffix);
		logger.info("after accounts");
		BankGenerator.date = new DateTime().minusDays(noOfDays).withTimeAtStartOfDay();
		BankGenerator.Timer transTimer = new BankGenerator.Timer();

		int totalTransactions = noOfTransactions * noOfDays;

		logger.info("Writing " + totalTransactions + " transactions for " + noOfCustomers
				+ " customers. suffix is " + key_suffix );
		int account_size = accounts.size();
		int transactionsPerAccount = noOfDays*noOfTransactions/account_size;
		logger.info("number of accounts generated is " + account_size + " transactionsPerAccount "
				+ transactionsPerAccount);
		List<Merchant> merchants = BankGenerator.createMerchantList();
		List<TransactionReturn> transactionReturns = BankGenerator.createTransactionReturnList();
		merchantRepository.createAll(merchants);
		logger.info("completed merchant next is transactionReturn");
		transactionReturnRepository.createAll(transactionReturns);
		CompletableFuture<SendResult<String, String>> kafka_cntr = null;
		CompletableFuture<Integer> transaction_cntr = null;
		int transactionIndex = 0;
		List<Transaction> transactionList = new ArrayList<>();
		for(Account account:accounts) {
			logger.info("writing account " + account.getAccountNo());
			for(int i=0; i<transactionsPerAccount; i++) {
				transactionIndex++;
				Transaction randomTransaction = BankGenerator.createRandomTransaction(noOfDays, transactionIndex, account, key_suffix,
							merchants, transactionReturns);
				if (doKafka)
					writeTransactionKafka(randomTransaction);
				else
					transaction_cntr = writeTransactionFuture(randomTransaction);
			}
		}
		if(!doKafka) {
			transaction_cntr.get();
		}
		transTimer.end();
		logger.info("Finished writing " + totalTransactions + " created in " +
				transTimer.getTimeTakenSeconds() + " seconds.");
	}

	private void writeTransactionKafka(Transaction randomTransaction) throws JsonProcessingException {
        try {
            String jsonStr = objectMapper.writeValueAsString(randomTransaction);
            topicProducer.send(jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    };

	//
	//  Account
	//
	public void saveSampleAccount() throws ParseException, RedisCommandExecutionException {
		Date create_date = new SimpleDateFormat("yyyy.MM.dd").parse("2010.03.28");
		logger.info("saveSampleAccount with create_date " + create_date.getTime());
		Account account = new Account("cust001", "acct001",
				"credit", "teller", "active",
				"ccnumber666655", Long.toString(create_date.getTime()),
				null, null, "jason", null);
		accountRepository.create(account);
	};

	public SearchResult getAccountTransactions(String account, Date startDate, Date endDate)
			throws ParseException, RedisCommandExecutionException {
		logger.info("in getAccountTransactions account is " + account);
		logger.info("startdate is " + startDate + " endDate is" + endDate);
		String tofromQuery = getDateToFromQueryString(startDate, endDate);
		String accountString = buildTagQuery("accountNo", account);
		String queryString = accountString + " " + tofromQuery;
		logger.info("query is " + queryString);

        return search(transactionSearchIndexName, queryString);
	};

	public SearchResult getCreditCardTransactions(String creditCard, Date startDate, Date endDate)
			throws ParseException, RedisCommandExecutionException {
		logger.info("credit card is " + creditCard + " start is " + startDate + " end is " + endDate);

		SearchResult transactionResults = null;
		String queryString = buildTagQuery("cardNum", creditCard);
		SearchResult accountResults = search(accountSearchIndexName, queryString);
		List<Document> docs = accountResults.getDocuments();
		//  result set has all accounts with a credit card
		//  build a query to match any of these merchants
		if(docs != null) {
			int i = 0;
			String accountListQueryString = "@accountNo:";
			for (Document document : docs) {
				if (i > 0) accountListQueryString = accountListQueryString + "|";
				String accountNo = (String) document.getId();
				String onlyID = accountNo.replace(accountSearchIndexName + ':', "");
				accountListQueryString = accountListQueryString + "{" + onlyID + "}";
				i += 1;
			}
			accountListQueryString = accountListQueryString;
			logger.info("accountListQueryString is " + accountListQueryString);
			String tofromQuery = getDateToFromQueryString(startDate, endDate);
			queryString = accountListQueryString + tofromQuery;
			logger.info("queryString is " + queryString);
			transactionResults = search(transactionSearchIndexName, queryString);
		}
		return transactionResults;
	};


	private  List<Account> createCustomerAccount(int noOfCustomers, String key_suffix) throws ExecutionException, InterruptedException, RedisCommandExecutionException {

		logger.info("Creating " + noOfCustomers + " customers with accounts and suffix " + key_suffix);
		BankGenerator.Timer custTimer = new BankGenerator.Timer();
		List<Account> accounts = null;
		List<Account> allAccounts = new ArrayList<>();
		List<Email> emails = null;
		List<Phone> phoneNumbers = null;
		CompletableFuture<Integer> account_cntr = null;
		CompletableFuture<Integer> customer_cntr = null;
		CompletableFuture<Integer> email_cntr = null;
		CompletableFuture<Integer> phone_cntr = null;
		int totalAccounts = 0;
		int totalEmails = 0;
		int totalPhone = 0;
		logger.info("before the big for loop");
		for (int i=0; i < noOfCustomers; i++){
			// logger.info("int noOfCustomer for loop i=" + i);
			Customer customer = BankGenerator.createRandomCustomer(key_suffix);
			List<Email> emailList = BankGenerator.createEmail(customer.getCustomerId());
			List<Phone> phoneList = BankGenerator.createPhone(customer.getCustomerId());
			for (Phone phoneNumber : phoneNumbers = phoneList) {
				phone_cntr = asyncService.writePhone(phoneNumber);
			}
			totalPhone = totalPhone + phoneNumbers.size();
			for (Email email: emails = emailList) {
				email_cntr = asyncService.writeEmail(email);
			}
			totalEmails = totalEmails + emails.size();
			accounts = BankGenerator.createRandomAccountsForCustomer(customer, key_suffix);
			totalAccounts = totalAccounts + accounts.size();
			for (Account account: accounts) {
				account_cntr = asyncService.writeAccounts(account);
			}
			customer_cntr = asyncService.writeCustomer(customer);
			if(!accounts.isEmpty()) {
				allAccounts.addAll(accounts);
			}
		}
		// logger.info("before the gets");
        assert account_cntr != null;
        account_cntr.get();
		assert customer_cntr !=null;
		customer_cntr.get();
		assert email_cntr != null;
		email_cntr.get();
		assert phone_cntr != null;
		phone_cntr.get();
		custTimer.end();
		logger.info("Customers=" + noOfCustomers + " Accounts=" + totalAccounts +
				" Emails=" + totalEmails + " Phones=" + totalPhone + " in " +
				custTimer.getTimeTakenSeconds() + " secs");
		return allAccounts;
	}

	//
	// TransactionReturns
	//

	public SearchResult getTransactionReturns() {
		logger.info("in getTransactionReturns ");
		String queryString = "*";

        return search(transactionReturnSearchIndexName, queryString);
	}
	//
	// Merchant
	//
	public SearchResult getMerchantCategoryTransactions(String in_merchantCategory, String account,
																		Date startDate, Date endDate) throws ParseException, RedisCommandExecutionException {
		String queryString = buildTagQuery("categoryCode", in_merchantCategory);
		SearchResult merchantResults = search(merchantSearchIndexName, queryString);
		SearchResult transactionResults = null;
		List<Document> docs = merchantResults.getDocuments();
		//  result set has all merchants with a category code
		//  build a query to match any of these merchants
		if(docs != null) {
			int i = 0;
			StringBuilder merchantListQueryString = new StringBuilder("@merchant:{");
			for (Document document : docs) {
				if (i > 0) merchantListQueryString.append("|");
				String merchantKey = (String) document.getId();
				logger.info(merchantKey);
				String onlyID = merchantKey.replace(merchantSearchIndexName + ':', "");
				logger.info(onlyID);
				merchantListQueryString.append(onlyID);
				i += 1;
			}
			merchantListQueryString = new StringBuilder(merchantListQueryString.append("}").toString());
			logger.info("merchantListQueryString is " + merchantListQueryString);
			String tofromQuery = getDateToFromQueryString(startDate, endDate);
			String accountString = buildTagQuery("accountNo", account);
			queryString = accountString + " " + merchantListQueryString + tofromQuery;
			logger.info("queryString is " + queryString);
			transactionResults = search(transactionSearchIndexName, queryString);
		}
		return transactionResults;
	}

	public SearchResult getMerchantTransactions (String in_merchant, String account, Date startDate, Date endDate)
			throws ParseException, RedisCommandExecutionException {
		logger.info("in getMerchantTransactions merchant is " + in_merchant + " and account is " + account);
		logger.info("startdate is " + startDate + " endDate is" + endDate);
		String tofromQuery = getDateToFromQueryString(startDate, endDate);
		String merchantString = buildTagQuery("merchant", in_merchant);
		String accountString = buildTagQuery("accountNo", account);
		String queryString =  merchantString + " " + accountString + " " + tofromQuery;
		logger.info("query is " + queryString);

        return search(transactionSearchIndexName, queryString);
	};

	private UnifiedJedis jedis_connection() {
		// Get the configuration from the application properties/environment
		UnifiedJedis unifiedJedis;
		String redisHost = "localhost"; // default name
		int redisPort = 6379;
		String redisPassword = "";

		redisHost = env.getProperty("redis.host", "localhost");
		redisPort = Integer.parseInt(env.getProperty("redis.port", "6379"));
		redisPassword = env.getProperty("spring.redis.password", "");

		ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
		poolConfig.setMaxIdle(50);
		poolConfig.setMaxTotal(50);
		HostAndPort hostAndPort = new HostAndPort(redisHost, redisPort);

		logger.info( "Host: " + redisHost + " Port " + String.valueOf(redisPort));
		if (!(redisPassword.isEmpty())) {
			String redisURL = "redis://:" + redisPassword + '@' + redisHost + ':' + String.valueOf(redisPort);
			logger.info("redisURL is " + redisURL);
			unifiedJedis = new JedisPooled(redisURL);
		} else {
			logger.info(" no password");
			unifiedJedis = new JedisPooled(hostAndPort);
		}
		return unifiedJedis;
	}
	private String buildTagQuery(String fieldName, String fieldValue) {
		return ("@" + fieldName + ":{" + fieldValue + "}");
	}


}
