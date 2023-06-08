#  easiest to look up a credit card using redinsight and edit this script to find an existing credit card
# date can be looked up and put in range
curl -X GET -H "Content-Type: application/json"  'http://localhost:8080/creditCardTransactions?creditCard=33c54179xb350x4b3dx9e96xa5be21c6539b&account=Acct1550J&from=2021-07-27&to=2021-09-30'
