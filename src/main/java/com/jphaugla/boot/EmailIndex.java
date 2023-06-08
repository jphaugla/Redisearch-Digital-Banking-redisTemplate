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
public class EmailIndex implements CommandLineRunner {

  @Autowired
  private StatefulRedisModulesConnection<String,String> connection;


  @Value("${app.emailSearchIndexName}")
  private String emailSearchIndexName;

  @Override
  @SuppressWarnings({ "unchecked" })
  public void run(String... args) throws Exception {
    RediSearchCommands<String,String> emailCommands = connection.sync();
    try {
      emailCommands.ftInfo(emailSearchIndexName);
    } catch (RedisCommandExecutionException rcee) {
      if (rcee.getMessage().equals("Unknown Index name")) {

        CreateOptions<String, String> options = CreateOptions.<String, String>builder()//
            .prefix(emailSearchIndexName + ':').build();

        Field emailAddress = Field.text("emailAddress").build();
        Field customerId = Field.text("customerId").build();
         emailCommands.ftCreate(
          emailSearchIndexName, //
          options, //
                emailAddress, customerId
        );
        log.info(">>>> Created " + emailSearchIndexName + " Search Index...");
      }
    }
  }

}
