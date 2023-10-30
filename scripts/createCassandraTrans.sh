export CQLSH_HOST=localhost
cqlsh -e "create keyspace if not exists banking with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } ;"
cqlsh < trans.cql
