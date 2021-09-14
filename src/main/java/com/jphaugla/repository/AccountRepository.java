package com.jphaugla.repository;

import com.jphaugla.domain.Account;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface AccountRepository extends CrudRepository<Account, String> {

	List<Account> getAccountsByCardNum(String creditCard);

}
