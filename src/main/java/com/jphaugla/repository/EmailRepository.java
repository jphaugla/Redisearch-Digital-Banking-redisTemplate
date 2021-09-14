package com.jphaugla.repository;

import com.jphaugla.domain.Email;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmailRepository extends CrudRepository<Email, String> {

	List<Email> getAccounts(String customer_id);

}
