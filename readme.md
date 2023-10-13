1. Start kakfa
`bin/zookeeper-server-start.sh config/zookeeper.properties`

`bin/kafka-server-start.sh config/server.properties`

3. Create Kafka topic
`bin/kafka-topics.sh --create --topic flink-kafka --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1`

2. (Optional) Kafka producer simulator
`bin/kafka-console-producer.sh --topic flink-kafka --bootstrap-server localhost:9092`

3. Run python fastAPI
`uvicorn app:app --host 0.0.0.0 --port 8000`

4. Run flink
`/usr/bin/env /usr/lib/jvm/java-11-openjdk-amd64/bin/java @/tmp/cp_d96odzbjpl55sq1kvndl8kz01.argfile com.example.ImageUploader`

5. Run Kafka input stream
`/usr/bin/env /usr/lib/jvm/java-11-openjdk-amd64/bin/java @/tmp/cp_axcy7mf97msderd28unyx91pc.argfile com.example.ImageDirectoryToKafkaProducer`
