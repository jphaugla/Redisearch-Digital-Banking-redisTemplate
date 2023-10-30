curl -X POST -H "Content-Type: application/json" --data '
{
  "name": "redis-sink-trans",
  "config": {
    "value.converter.schemas.enable": "false",
    "name": "redis-sink-trans",
    "connector.class": "com.redis.kafka.connect.RedisSinkConnector",
    "tasks.max": "1",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "transforms": "Cast",
    "topics": "transactions",
    "transforms.Cast.type": "org.apache.kafka.connect.transforms.Cast$Key",
    "transforms.Cast.spec": "string",
    "redis.uri": "redis://redis-15229.int.jph.jphaugla.demo-rlec.redislabs.com:15229",
    "redis.password": "redis123",
    "redis.type": "HASH",
    "redis.key": "Trans",
    "redis.separator": ":"
  }
}
} ' http://172.172.133.201:8083/connectors -w "\n"
