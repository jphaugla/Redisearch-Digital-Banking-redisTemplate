package com.jphaugla.boot;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;


import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.lettuce.core.RedisCommandExecutionException;


import lombok.extern.slf4j.Slf4j;

@Component
@Order(6)
@Slf4j
public class TransactionIndex implements CommandLineRunner {

  @Autowired
  private StatefulRedisModulesConnection<String,String> connection;

  @Value("${app.transactionSearchIndexName}")
  private String transactionSearchIndexName;


  @Override
  @SuppressWarnings({ "unchecked" })
  public void run(String... args) throws Exception {

    RediSearchCommands<String,String> transactionCommands = connection.sync();

    try {
      transactionCommands.ftInfo(transactionSearchIndexName);
    } catch (RedisCommandExecutionException rcee) {
      if (rcee.getMessage().equals("Unknown Index name")) {

        CreateOptions<String, String> options = CreateOptions.<String, String>builder()//
            .prefix(transactionSearchIndexName + ':').build();

        Field accountNo = Field.text("accountNo").build();
        Field amountType = Field.text("amountType").build();
        Field merchant = Field.text("merchant").build();
        Field status = Field.text("status").sortable().build();
        Field description = Field.text("description").build();
        Field referenceKeyType = Field.text("referenceKeyType").build();
        Field referenceValue = Field.text("referenceValue").build();
        Field tranCd = Field.text("tranCd").build();
        Field location = Field.text("location").build();
        Field transactionReturn = Field.text("transactionReturn").build();
        Field initialDate = Field.numeric("initialDate").sortable().build();
        Field settlementDate = Field.numeric("settlementDate").sortable().build();
        Field postingDate = Field.numeric("postingDate").sortable().build();
        Field transactionTags = Field.tag("transactionTags").separator(':').sortable().build();

         transactionCommands.ftCreate(
          transactionSearchIndexName, //
          options, //
                 accountNo, amountType, merchant, status, description, referenceKeyType, tranCd, location, transactionReturn, referenceValue, initialDate, settlementDate, postingDate,transactionTags
        );

        log.info(">>>> Created " + transactionSearchIndexName + " Search Index...");
      }
    }
  }

}
