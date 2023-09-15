package com.sj11.kafka.backup.service.model

import cats.implicits._
import com.sj11.kafka.backup.service.RecordBinary
import fs2.kafka.ConsumerRecord

import scala.util.Try

case class RecordBackup(topic: String, partition: Int, offset: Long, content: Array[Byte])

object RecordBackup {
  def from(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): Try[RecordBackup] = Try {
    val binary = RecordBinary.from(consumerRecord).toByteArray
    RecordBackup(consumerRecord.topic, consumerRecord.partition, consumerRecord.offset, binary)
  }.onError { case e =>
    Try(println(s"toBinary has exploded. Error: ${e.getLocalizedMessage}"))
  }

  def from(topic: String, partition: Int, record: RecordBinary): Try[RecordBackup] = Try {
    RecordBackup(topic, partition, record.offset, record.toByteArray)
  }.onError { case e =>
    Try(println(s"toBinary has exploded. Error: ${e.getLocalizedMessage}"))
  }
}
