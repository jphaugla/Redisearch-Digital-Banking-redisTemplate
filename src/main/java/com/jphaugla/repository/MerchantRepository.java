package com.jphaugla.repository;

import com.jphaugla.domain.Merchant;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MerchantRepository extends CrudRepository<Merchant, String> {

	List<Merchant> findByCategoryCode(String categoryCode);

	List<Merchant> findByCategoryDescription(String categoryDescription);

	List<Merchant> findByState(String state);

};

