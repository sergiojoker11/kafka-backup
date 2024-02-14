package com.sj11.kafka.backup.service.config

import com.sj11.kafka.backup.kafka.model.ProducerConfig
import com.typesafe.config.{Config, ConfigFactory}

import java.util.UUID

case class RestoreConfig(service: ServiceConfig, producer: ProducerConfig)
object RestoreConfig {

  private val config: Config = ConfigFactory.load().getConfig("restore")
  private val kafkaConfig = config.getConfig("kafka")
  private val serviceImplConfig = ServiceConfig(config.getConfig("service"))
  private val producerConfig = ProducerConfig(
    schemaRegistryUrl = kafkaConfig.getString("schema-registry-url"),
    schemaRegistryUsername = kafkaConfig.getString("schema-registry-username"),
    schemaRegistryPassword = kafkaConfig.getString("schema-registry-password"),
    kafkaBootstrap = kafkaConfig.getString("bootstrap-servers"),
    sslEnabled = kafkaConfig.getBoolean("ssl-enabled"),
    clientId = s"${kafkaConfig.getString("client-id-prefix")}-${UUID.randomUUID()}",
    noRetries = kafkaConfig.getInt("number-of-retries"),
    truststoreLocation = kafkaConfig.getString("truststore-location"),
    keystoreLocation = kafkaConfig.getString("keystore-location"),
    keystoreType = kafkaConfig.getString("keystore-type"),
    keysPassword = kafkaConfig.getString("keys-password"),
    strategy = "TopicRecordNameStrategy"
  )

  val get = RestoreConfig(serviceImplConfig, producerConfig)

}
