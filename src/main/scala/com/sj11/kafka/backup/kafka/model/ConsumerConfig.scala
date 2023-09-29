package com.sj11.kafka.backup.kafka.model

import com.typesafe.config.ConfigFactory
import fs2.kafka.AutoOffsetReset

import java.time.Instant

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

object ConsumerConfig {

  private val kafkaConfig = ConfigFactory.load().getConfig("service.kafka")
  private val now = Instant.now()

  def get = ConsumerConfig(
    schemaRegistryUrl = kafkaConfig.getString("schema-registry-url"),
    schemaRegistryUsername = kafkaConfig.getString("schema-registry-username"),
    schemaRegistryPassword = kafkaConfig.getString("schema-registry-password"),
    kafkaBootstrap = kafkaConfig.getString("bootstrap-servers"),
    sslEnabled = kafkaConfig.getBoolean("ssl-enabled"),
    keysPassword = kafkaConfig.getString("keys-password"),
    groupId = s"${kafkaConfig.getString("group-id-prefix")}-$now",
    topicRegex = kafkaConfig.getString("topic-regex"),
    clientId = s"${kafkaConfig.getString("client-id-prefix")}-$now",
    truststoreLocation = kafkaConfig.getString("truststore-location"),
    keystoreLocation = kafkaConfig.getString("keystore-location"),
    keystoreType = kafkaConfig.getString("keystore-type"),
    autoOffsetReset = AutoOffsetReset.Latest
  )
}
