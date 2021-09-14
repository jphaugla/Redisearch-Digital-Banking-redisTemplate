# for server testing to generate higher load levels.  
# Use with startAppservers.sh 
nohup curl 'http://localhost:8080/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=J&pipelined=false' > /tmp/generateJ.out 2>&1 &
nohup curl 'http://localhost:8081/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=P&pipelined=false' > /tmp/generateP.out 2>&1 &
nohup curl 'http://localhost:8082/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=H&pipelined=false' > /tmp/generateH.out 2>&1 &
nohup curl 'http://localhost:8083/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=C&pipelined=false' > /tmp/generateC.out 2>&1 &
nohup curl 'http://localhost:8084/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=A&pipelined=false' > /tmp/generateA.out 2>&1 &
nohup curl 'http://localhost:8085/generateData?noOfCustomers=5000&noOfTransactions=100000&noOfDays=10&key_suffix=G&pipelined=false' > /tmp/generateG.out 2>&1 &
