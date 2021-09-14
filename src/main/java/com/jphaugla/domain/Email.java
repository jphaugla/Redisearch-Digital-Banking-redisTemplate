package com.jphaugla.domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("Email")
public class Email {
        private @Id String emailAddress;
        private String emailLabel;
        private String customerId;

}
