package com.sj11.kafka.backup.kafka.model

case class ProducerConfig(
  schemaRegistryUrl: String,
  schemaRegistryUsername: String,
  schemaRegistryPassword: String,
  kafkaBootstrap: String,
  sslEnabled: Boolean,
  clientId: String,
  noRetries: Int,
  truststoreLocation: String,
  keystoreLocation: String,
  keystoreType: String,
  keysPassword: String,
  strategy: String)
  extends Ssl
