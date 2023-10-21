# redis enterprise example
# export REDIS_HOST=redis-17794.jph.jphaugla.demo-rlec.redislabs.com
# export REDIS_PORT=17794
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=jasonrocks
export USE_SSL=false
# substitue in actual KAFKA_HOST internal IP
# this will not run using a public ip.  Did not set up the advertized listeners in kafka to make this work
export KAFKA_HOST=localhost
export KAFKA_PORT=9092
