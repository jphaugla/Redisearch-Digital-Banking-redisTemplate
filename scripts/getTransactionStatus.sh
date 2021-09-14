# see count of transactions by account status of PENDING, AUTHORIZED, SETTLED
curl -X GET -H "Content-Type: application/json"  'http://localhost:8080/transactionStatusReport'
