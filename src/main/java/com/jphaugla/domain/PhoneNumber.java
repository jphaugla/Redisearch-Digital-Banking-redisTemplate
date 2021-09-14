package com.jphaugla.domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Data
@AllArgsConstructor
@NoArgsConstructor

@RedisHash("PhoneNumber")
public class PhoneNumber {

        private @Id String phoneNumber;
        private String phoneLabel;
        private String customerId;
}
