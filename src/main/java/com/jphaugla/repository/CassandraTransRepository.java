package com.jphaugla.repository;

import com.jphaugla.domain.CassandraTransaction;
import org.springframework.data.cassandra.repository.CassandraRepository;


import java.util.UUID;

public interface CassandraTransRepository extends CassandraRepository<CassandraTransaction, String> {

}