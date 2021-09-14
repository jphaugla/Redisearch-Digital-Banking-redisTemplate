package com.jphaugla.domain;

import lombok.*;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@RedisHash("Account")


public class Account {
    private @Id String accountNo;
    private String customerId;
    private String accountType;
    private String accountOriginSystem;
    private String accountStatus;
    private String cardNum;
    private Date openDate;
    private Date lastUpdated;
    private String lastUpdatedBy;
    private String createdBy;
    private Date createdDate;
}
