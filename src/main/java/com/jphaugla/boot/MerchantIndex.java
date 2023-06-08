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
public class MerchantIndex implements CommandLineRunner {


  @Autowired
  private StatefulRedisModulesConnection<String,String> connection;


  @Value("${app.merchantSearchIndexName}")
  private String merchantSearchIndexName;

  @Override
  @SuppressWarnings({ "unchecked" })
  public void run(String... args) throws Exception {
    RediSearchCommands<String,String> merchantCommands = connection.sync();
    try {
      merchantCommands.ftInfo(merchantSearchIndexName);
    } catch (RedisCommandExecutionException rcee) {
      if (rcee.getMessage().equals("Unknown Index name")) {

        CreateOptions<String, String> options = CreateOptions.<String, String>builder()//
            .prefix(merchantSearchIndexName + ':').build();

        Field merchantName = Field.text("name").build();
        Field categoryCode = Field.text("categoryCode").build();
        Field categoryDescription = Field.text("categoryDescription").build();
        Field merchantState = Field.text("state").build();
        Field merchantCountry = Field.text("countryCode").build();
        merchantCommands.ftCreate(
          merchantSearchIndexName, //
          options, //
                merchantName, categoryCode, categoryDescription, merchantState, merchantCountry
        );
        log.info(">>>> Created " + merchantSearchIndexName + " Search Index...");
      }
    }
  }

}
