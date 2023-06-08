package com.jphaugla.boot;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
@Slf4j
public class CustomerIndex implements CommandLineRunner {

  @Autowired
  private StatefulRedisModulesConnection<String,String> connection;


  @Value("${app.customerSearchIndexName}")
  private String customerSearchIndexName;

  @Override
  @SuppressWarnings({ "unchecked" })
  public void run(String... args) throws Exception {
    RediSearchCommands<String,String> customerCommands = connection.sync();
    try {
      customerCommands.ftInfo(customerSearchIndexName);
    } catch (RedisCommandExecutionException rcee) {
      if (rcee.getMessage().equals("Unknown Index name")) {

        CreateOptions<String, String> options = CreateOptions.<String, String>builder()//
            .prefix(customerSearchIndexName + ':').build();

        Field city = Field.text("city").build();
        Field firstName = Field.text("firstName").build();
        Field fullName = Field.text("fullName").build();
        Field lastName = Field.text("lastName").build();
        Field stateAbbreviation = Field.text("stateAbbreviation").build();
        Field zipcode = Field.text("zipcode").build();
        Field customerId = Field.text( "customerId").build();

        customerCommands.ftCreate(
          customerSearchIndexName, //
          options, //
                customerId, city, firstName, fullName, lastName, stateAbbreviation, zipcode
        );

        log.info(">>>> Created " + customerSearchIndexName + " Search Index...");
      }
    }
  }

}
