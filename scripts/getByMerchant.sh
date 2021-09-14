# find all transactions for an account from one merchant in date range
curl -X GET -H "Content-Type: application/json"  'http://localhost:8080/merchantTransactions/?merchant=BestBuy&account=Acct1380J&from=2021-06-27&to=2021-09-01'
