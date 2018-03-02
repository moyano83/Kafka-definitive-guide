# Kafka the definitive guide

## Table of contents:
1. [Chapter 1: Meet Kafka](#Chapter1)
2. [Chapter 2: Installing Kafka](#Chapter2)

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
