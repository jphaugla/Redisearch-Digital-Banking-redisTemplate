package com.jphaugla.repository;

import com.jphaugla.domain.PhoneNumber;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhoneRepository extends CrudRepository<PhoneNumber, String> {

	List<PhoneNumber> getAccounts(String customer_id);

}
