export CQLSH_HOST=74.235.229.140
cqlsh -e "create keyspace if not exists banking with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } ;"

cqlsh -e "create table if not exists banking.transactions ( tranId text, accountNo text, amountType text, merchant text, referenceKeyType text, referenceKeyValue text, originalAmount text, amount text, tranCd  text, description text, initialDate text, settlementDate text, postingDate text, status    text, disputeId text, transactionReturn text, location text, transactionTags text, primary key (tranId));"
