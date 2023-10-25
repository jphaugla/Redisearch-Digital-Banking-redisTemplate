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
    "redis.uri": "redis://redis-12384.int.jph.jphaugla.demo-rlec.redislabs.com:12384",
    "redis.password": "********",
    "redis.type": "HASH",
    "redis.key": "Trans",
    "redis.separator": ":"
  }
}
} ' http://localhost:8083/connectors -w "\n"
