export CQLSH_HOST=74.235.229.140
cqlsh -e "create keyspace if not exists banking with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } ;"
cqlsh < trans.cql
