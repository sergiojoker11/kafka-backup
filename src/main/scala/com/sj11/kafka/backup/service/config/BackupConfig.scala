package com.sj11.kafka.backup.service.config

import com.sj11.kafka.backup.kafka.model.ConsumerConfig
import com.typesafe.config.{Config, ConfigFactory}
import fs2.kafka.AutoOffsetReset

import java.time.Instant

case class BackupConfig(service: ServiceConfig, consumer: ConsumerConfig)
object BackupConfig {

  private val config: Config = ConfigFactory.load().getConfig("backup")
  private val kafkaConfig = config.getConfig("kafka")
  private val now = Instant.now()
  private val serviceImplConfig = ServiceConfig(config.getConfig("service"))
  private val consumerConfig = ConsumerConfig(
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

  val get = BackupConfig(serviceImplConfig, consumerConfig)
}
