package com.sj11.kafka.backup.service.model

import org.apache.kafka.clients.producer.RecordMetadata

case class RecordRestoreResult(topic: String, partition: Int, offset: Long, timestamp: Long)

object RecordRestoreResult {
  def from(r: RecordMetadata): RecordRestoreResult =
    RecordRestoreResult(r.topic(), r.partition(), r.offset(), r.timestamp())
}
