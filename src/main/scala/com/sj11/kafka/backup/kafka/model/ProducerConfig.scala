package com.sj11.kafka.backup.kafka.model

import com.typesafe.config.ConfigFactory

import java.util.UUID

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
  topic: String,
  strategy: String)
  extends Ssl
object ProducerConfig {

  private val kafkaConfig = ConfigFactory.load().getConfig("service.kafka")

  def get = ProducerConfig(
    schemaRegistryUrl = kafkaConfig.getString("schema-registry-url"),
    schemaRegistryUsername = kafkaConfig.getString("schema-registry-username"),
    schemaRegistryPassword = kafkaConfig.getString("schema-registry-password"),
    kafkaBootstrap = kafkaConfig.getString("bootstrap-servers"),
    sslEnabled = kafkaConfig.getBoolean("ssl-enabled"),
    clientId = s"${kafkaConfig.getString("client-id")}-${UUID.randomUUID()}",
    noRetries = kafkaConfig.getInt("number-of-retries"),
    truststoreLocation = kafkaConfig.getString("truststore-location"),
    keystoreLocation = kafkaConfig.getString("keystore-location"),
    keystoreType = kafkaConfig.getString("keystore-type"),
    keysPassword = kafkaConfig.getString("keys-password"),
    topic = "n/a",
    strategy = "TopicRecordNameStrategy"
  )
}
