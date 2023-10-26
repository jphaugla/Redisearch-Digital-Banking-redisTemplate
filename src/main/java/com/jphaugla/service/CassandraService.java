package com.jphaugla.service;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.jphaugla.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
@Slf4j
@Service
public class CassandraService {
    CqlSession session;
    private String cassandraHost;
    CassandraService() {
        log.info("cassandra constructor");
        CqlSession session = CqlSession.builder().build();
    }
    public Transaction getTransaction (String transactionId) {
        log.info("in get transaction with tranId=" + transactionId);
        Transaction returnTransaction = new Transaction();
        String query = "select * from banking.transactions where tranId=" + transactionId;
        ResultSet rs = session.execute(query);
        Row row = rs.one();
        // process the row
        if (row != null) {
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("tranId")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("accountNo")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("amountType")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("merchant")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("referenceKeyType")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("referenceKeyValue")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("originalAmount")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("amount")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("tranCd")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("description")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("initialDate")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("settlementDate")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("postingDate")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("status")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("disputeId")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("transactionReturn")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("location")));
            returnTransaction.setTranId(row.getString(CqlIdentifier.fromCql("transactionTags")));
            log.info("found in cassandra");
        }
        return returnTransaction;
    }
}
