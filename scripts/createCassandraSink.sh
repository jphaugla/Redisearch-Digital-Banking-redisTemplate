curl -X POST -H "Content-Type: application/json" --data '
{
  "name": "cassandra-sink",
  "config": {
    "topic.banking.transactions.views.mapping": "accountNo=value.accountNo, amountType=value.amountType, merchant=value.merchant, referenceKeyType=value.referenceKeyType, referenceKeyValue=value.referenceKeyValue, originalAmount=value.originalAmount, amount=value.amount, tranCd=value.tranCd, description=value.description, initialDate=value.initialDate, settlementDate=value.settlementDate, postingDate=value.postingDate, status=value.status, disputeId=value.disputeId, transactionReturn=value.transactionReturn, location=value.location, transactionType=value.transactionType",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "name": "cassandra-sink",
    "connector.class": "com.datastax.kafkaconnector.DseSinkConnector",
    "tasks.max": "1",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "topics": "transactions",
    "contactPoints": "10.0.11.8",
    "loadBalancing.localDc": "datacenter1"
  }
}
} ' http://localhost:8083/connectors -w "\n"
