# Kafka the definitive guide

## Table of contents:
1. [Chapter 1: Meet Kafka](#Chapter1)
2. [Chapter 2: Installing Kafka](#Chapter2)
3. [Chapter 3: Kafka Producers: Writing Messages to Kafka](#Chapter3)

## Chapter 1: Meet Kafka<a name="Chapter1"></a>
### Publish/Subscribe Messaging
Publish/subscribe messaging is a pattern that is characterized by the sender (publisher) of a piece of data (message) not specifically directing it to a receiver. Instead, the publisher classifies the message somehow, and that receiver (subscriber) subscribes to receive certain classes of messages.

### Enter Kafka
Kafka is a publish/subscribe messaging system often described as a _distributed commit log_ or _distributing streaming platform_. A filesystem or database commit log is designed to provide a durable record of all transactions so that they can be replayed to consistently build the state of a system, data within Kafka is stored durably, in order, and can be read deterministically.
The unit of data within kafka is the _message_, and it is treated as an array of bytes, a message can have an optional bit of metadata, which is referred to as a key (also an array of bytes). Messages are written into Kafka in batches (typically compressed), a batch is just a collection of messages, all of which are being produced to the same topic and partition.

### Schemas
While messages are opaque byte arrays to Kafka itself, it is recommended that additional structure, or schema, be imposed on the message content so that it can be easily understood. Many Kafka developers favor the use of Apache Avro, which provides a compact serialization format; schemas that are separate from the message pay‐ loads and that do not require code to be generated when they change; and strong data typing and schema evolution, with both backward and forward compatibility.

### Topics and Partitions
Messages in Kafka are categorized into topics (analogous to a table in a DB), and broken down into partitions which are append only logs read in order from the beginning to the end, order is only guaranteed in the partitions, but not in the topics.

### Producers and Consumers
Producers write messages to topics, but can also write messages to partitions by using the message key and a partitioner. Consumers read messages, they subscribe to one or more topics and read messages in order, keeping track of the messages that has already been consumed (by means of offset which is a bit of metadata). A consumer works in a consumer group, one or more consumers that works together to consume a topic. The group assures that each partition is only consumed by one member.

### Brokers and Clusters
A single Kafka server is called a broker, it receives messages from producers, assigns offsets to them, and commits the messages to storage on disk. It also services consumers, responding to fetch requests for partitions and responding with the mes‐ sages that have been committed to disk.
Kafka servers works in clusters, which has a _controller_ (elected from one of the nodes) responsible for administrative operations. A partition is handled by a single broker in the cluster (leader of the partition), although the partition can be replicated in multiple brokers. Messages are stored in the brokers by default for 7 days (retention), or until the topic reaches certain size (default to 1GB).

## Chapter 2: Installing Kafka<a name="Chapter2"></a>
### Installing zookeeper
Kafka requires java 8 and zookeeper, you can check the zookeeper status by connecting to the server/port and sending the command `srvr`. A Zookeeper cluster is called an ensemble, which is recommended to have an odd number of nodes (3,5 or 7 as maximum). The nodes in zookeeper must have a common configuration, example:

```text
    tickTime=2000
    dataDir=/var/lib/zookeeper
    clientPort=2181
    # Amount of time to allow followers to connect with a leader, measured in tickTime units (40s in this case)
    initLimit=20 
    # How out-of-sync followers can be with the leader, measured in tickTime units (10s in this case)
    syncLimit=5
    # hostname:peerPort(port over which servers in the ensemble communicate with each other):leaderPort(port over which leader election is performed)
    server.1=zoo1.example.com:2888:3888
    server.2=zoo2.example.com:2888:3888
    server.3=zoo3.example.com:2888:3888
```

### Installing kafka broker
there is several scripts to start a broker, produce and consume messages in <kafka_dir>/bin.

Start the server with: `<kafka_dir>/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties`
Create and verify a topic: `<kafka_dir>/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test`
Produce messages to a test topic: `<kafka_dir>/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test`
Consume messages from a test topic: `<kafka_dir>/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning`

### Broker Configuration
#### General Broker

    * broker.id: Integer corresponding to the broker identifier. Must be unique within a single kafka cluster
    * port: The kafka port, can be assigned randomly, but if the port is lower than 1024 kafka must be started as root
    * zookeeper.connect: Zookeeper address on the form hostname:port/path where path is an optional Zookeeper path to use as a chroot environment for the Kafka cluster
    * log.dirs: Kafka persists all messages to disk, stored in the path specified by this comma separated list of paths 
    * num.recovery.threads.per.data.dir: Configurable pool of threads to handle log segments. Only one thread per log directory is used by default
    * auto.create.topics.enable: By default Kafka automatically create a topic under the following circumstances (set to false to deactivate it)
        - When a producer starts writing messages to the topic
        - When a consumer starts reading messages from the topic
        - When any client requests metadata for the topic

#### Topic Defaults

    * num.partitions: Number of partitions a new topic is created with (defaults 1). The number of partitions for a topic can only be increased, never decreased. Some considerations you should have to choose the number of partitions:
        - Size of the writes per second
        - Maximum throughput per consumer from a single partition
        - Maximum throughput per producer from a single partition (safe to skip as it is usually limited by consumers and not producers)
        - If you are sending messages to partitions based on keys, calculate throughput based on your expected future usage, not the current usage
        - Consider the number of partitions on each broker and available diskspace and network bandwidth per broker
        - Avoid overestimating, each partition uses memory and resources
     Experience suggests that limiting the size of the partition on the disk to less than 6 GB per day of retention  often gives satisfactory results
    * log.retention.ms/log.retention.minutes/log.retention.hours: Amount of time after which messages may be deleted (the smaller unit size will take precedence if more than one is specified)
    * log.retention.bytes: Applied per partition, total number of bytes of messages retained
    * log.segment.bytes: As messages are produced to the Kafka broker, they are appended to the current log segment for the partition. Once the log segment has reached the size specified by the log.segment.bytes parameter, which defaults to 1 GB, the log segment is closed and a new one is opened. Once a log segment has been closed, it can be considered for expiration
    * log.segment.ms: Specifies the amount of time after which a log segment should be closed
    * message.max.bytes: Maximum size of a message that can be produced (defaults to 1MB) A producer that tries to send a message larger than this will receive an error, this value must be coordinated with the fetch.message.max.bytes configuration on consumer clients
    
### Hardware Selection
Several factors that will contribute to the overall performance: disk throughput and capacity, memory, networking, and CPU.
 
    * Disk Throughput: Faster disk writes will equal lower produce latency. SSD will usually have better performance
    * Disk Capacity: Depends on the retention configuration. Consider a 10% of overhead or other files appart of the capacity to store log messages
    * Memory: Consumers reads from the end of the partitions, where the consumer is caught up and lagging behind the producers very little, if at all. The messages the consumer is reading are optimally stored in the system’s page cache, having more memory available to the system for page cache will improve the performance of consumer clients
    * Networking: The available network throughput will specify the maximum amount of traffic that Kafka can handle. Cluster replication and mirroring will also increase requirements
    * CPU: Ideally, clients should compress messages to optimize network and disk usage. The Kafka broker must decompress all message batches, however, in order to validate the checksum of the individual messages and assign offsets
    
### Kafka Clusters
Considerations on configuring a kafka cluster:

    * Number of brokers: Depends on the overall disk capacity, capacity of network interfaces...
    * Broker configuration: All brokers must have the same config‐ uration for the _zookeeper.connect_ parameter and all brokers must have a unique value for the _broker.id_
    * OS Tuning: Few configuration changes can be made to improve kafka performance
        - Virtual memory: Try to avoid memory swaps. Disable it, or set vm.swappiness parameter to a very low value, such as 1. In regards to I/O the number of dirty pages that are allowed, before the flush background process starts writing them to disk, can be reduced by setting the =vm.dirty_background_ratio value lower than the default of 10. The total number of dirty pages that are allowed before the kernel forces synchronous operations to flush them to disk can also be increased by changing the value of vm.dirty_ratio (defaults 20) 
        - Disk: The Extents File System (XFS) is used in most of linux distributions due to performance reasons.
        - Networking: The recommended changes for Kafka are the same as those suggested for most web servers and 
        other networking applications. The first adjustment is to change the default and maximum amount of memory allocated for the send and receive buffers for each socket (_net.core.wmem_default_, _net.core.rmem_default_, _net.core.wmem_max_ and net.core.rmem_max). The send and receive buffer sizes for TCP sockets must be set separately using the _net.ipv4.tcp_wmem_ and _net.ipv4.tcp_rmem_ parameters, the maximum size cannot be larger than the values specified for all sockets.

### Production Concerns
#### Garbage Collector Options
Garbage First (or G1) garbage collector is designed to automatically adjust to different workloads and provide consis‐ tent pause times for garbage collection over the lifetime of the application. Some options:

    * MaxGCPauseMillis: Preferred pause time for each garbage-collection cycle (not a hard limit, can be exceeded)
    * InitiatingHeapOccupancyPercent: Percentage of the total heap that may be in use before G1 will start a collection cycle
    
The start script for Kafka does not use the G1 collector, instead defaulting to using parallel new and concurrent mark and sweep garbage collection. To use G1 do:
```bash
export JAVA_HOME=/usr/java/jdk1.8.0_51
export KAFKA_JVM_PERFORMANCE_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -Djava.awt.headless=true"
/usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties
```

### Colocating Applications on Zookeeper
Kafka utilizes Zookeeper for storing metadata information about the brokers, topics, and partitions. Writes to Zookeeper are only performed on changes to the member‐ ship of consumer groups or on changes to the Kafka cluster itself (does not justify a dedicated zookeeper ensemble). Consumers have a configurable choice to use either Zookeeper or Kafka for committing offsets. These commits can be a significant amount of Zookeeper traffic, especially in a cluster with many consumers, and will need to be taken into account.

## Chapter 3: Kafka Producers: Writing Messages to Kafka<a name="Chapter3"></a>