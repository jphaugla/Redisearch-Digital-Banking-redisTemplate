package com.jphaugla.domain;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;


import java.io.Serializable;
import java.util.Date;
@Data

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter


public class Transaction implements Serializable {
    private String tranId;
    private String accountNo;
    // debit or credit
    private String amountType;
    private String merchant;
    private String referenceKeyType;
    private String referenceKeyValue;
    private String originalAmount;
    private String amount;
    private String tranCd ;
    private String description;
    private String initialDate;
    private String settlementDate;
    private String postingDate;
    //  this is authorized, posted, settled
    private String status   ;
    private String disputeId;
    private String transactionReturn;
    private String location;
    private String transactionTags;

}
