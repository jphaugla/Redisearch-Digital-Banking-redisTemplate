package com.jphaugla.repository;


import com.jphaugla.domain.Transaction;
import org.springframework.data.repository.CrudRepository;


import java.util.Date;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, String> {

	List<Transaction> findByMerchantAndAccountNo(String merchant, String accountNo);

	// does not work to query between in redis
	List<Transaction> findByMerchantAndAccountNoAndPostingDateBetween(String merchant,
																	  String accountNo, Date startDate, Date endDate);

	List<Transaction> findByTransactionReturn(String transactionReturn);
	List<Transaction> findByStatus(String transactionStatus);
	//  cannot do two columns as will get cross-slot error on the intersection
	List<Transaction> findByStatusAndAcctNo(String transactionStatus, String accountNo);
	//  cannot do two columns as will get cross-slot error on the intersection
	List<Transaction> findByMerchantAccount(String transactionStatus, String merchant);


}
