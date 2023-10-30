curl -X POST -H "Content-Type: application/json" --data '
{
  "name": "cassandra-sink-trans",
  "config": {
    "topic.transactions.banking.transaction.mapping": "tranid=value.tranid, accountno=value.accountno, amounttype=value.amounttype, merchant=value.merchant, referencekeytype=value.referencekeytype, referencekeyvalue=value.referencekeyvalue, originalamount=value.originalamount, amount=value.amount, trancd=value.trancd, description=value.description, initialdate=value.initialdate, settlementdate=value.settlementdate, postingdate=value.postingdate, status=value.status, disputeid=value.disputeid, transactionreturn=value.transactionreturn, location=value.location, transactiontags=value.transactiontags",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "name": "cassandra-sink-trans",
    "connector.class": "com.datastax.kafkaconnector.DseSinkConnector",
    "tasks.max": "1",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "topics": "transactions",
    "contactPoints": "10.0.11.5",
    "loadBalancing.localDc": "datacenter1"
  }
}
} ' http://172.172.133.201:8083/connectors -w "\n"
