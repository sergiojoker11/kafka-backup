package com.sj11.kafka.backup.kafka.model

import org.apache.kafka.clients.CommonClientConfigs

import scala.collection.mutable

trait Ssl {
  def sslEnabled: Boolean

  def truststoreLocation: String

  def keystoreLocation: String

  def keystoreType: String

  def keysPassword: String
}

object Ssl {
  def apply(config: Ssl): Map[String, String] = {
    val base = mutable.Map.empty[String, String]
    if (config.sslEnabled) {
      base.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      base.put("ssl.truststore.location", config.truststoreLocation)
      base.put("ssl.truststore.password", config.keysPassword)
      base.put("ssl.keystore.type", config.keystoreType)
      base.put("ssl.keystore.location", config.keystoreLocation)
      base.put("ssl.keystore.password", config.keysPassword)
      base.put("ssl.key.password", config.keysPassword)
    }
    base.toMap
  }
}
