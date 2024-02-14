package com.sj11.kafka.backup.kafka.model

import fs2.kafka.AutoOffsetReset

case class ConsumerConfig(
  schemaRegistryUrl: String,
  schemaRegistryUsername: String,
  schemaRegistryPassword: String,
  kafkaBootstrap: String,
  sslEnabled: Boolean,
  keysPassword: String,
  groupId: String,
  topicRegex: String,
  clientId: String,
  truststoreLocation: String,
  keystoreLocation: String,
  keystoreType: String,
  autoOffsetReset: AutoOffsetReset)
  extends Ssl
