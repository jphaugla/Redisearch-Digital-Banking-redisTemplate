# find all transactions for an account from merchant category for date range
curl -X GET -H "Content-Type: application/json"  'http://localhost:8080/merchantCategoryTransactions/?merchantCategory=5732&account=Acct1067J&from=2021-07-27&to=2021-08-30'
