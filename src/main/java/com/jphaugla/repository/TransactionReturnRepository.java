package com.jphaugla.repository;

import com.jphaugla.domain.TransactionReturn;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionReturnRepository extends CrudRepository<TransactionReturn, String> {

	List<TransactionReturn> getAccounts(String customer_id);

}
