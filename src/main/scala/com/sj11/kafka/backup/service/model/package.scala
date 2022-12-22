package com.sj11.kafka.backup.service

import cats.implicits.catsSyntaxOptionId
import fs2.kafka.{ConsumerRecord, Timestamp}

case class RecordBackup(topic: String, partition: Int, offset: Long, content: Array[Byte])

case class RecordBinary(
  offset: Long,
  timestamp: TimestampBinary,
  key: Array[Byte],
  value: Array[Byte],
  headers: List[HeaderRecordBinary])

object RecordBinary {
  def from(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): RecordBinary = {
    RecordBinary(
      offset = consumerRecord.offset,
      timestamp = TimestampBinary.from(consumerRecord.timestamp),
      key = consumerRecord.key,
      value = consumerRecord.value,
      headers = consumerRecord.headers.toChain.map(h => HeaderRecordBinary(h.key().getBytes, h.value())).toList
    )
  }
}

case class HeaderRecordBinary(key: Array[Byte], value: Array[Byte])
trait TimestampType

object CreateTime extends TimestampType
object LogAppendTime extends TimestampType
object UnknownTime extends TimestampType
object NoTime extends TimestampType

object TimestampType {
  def apply(value: Int): TimestampType = value match {
    case 1 => CreateTime
    case 2 => LogAppendTime
    case 3 => UnknownTime
    case -1 => NoTime
  }
}

case class TimestampBinary(`type`: Int, value: Option[Long])

object TimestampBinary {
  def from(t: Timestamp): TimestampBinary = {
    val timestampOpt = t.createTime
      .map(t => (CreateTime, t))
      .orElse(t.logAppendTime.map(t => (LogAppendTime, t)))
      .orElse(t.unknownTime.map(t => (UnknownTime, t)))
    timestampOpt match {
      case Some((CreateTime, t)) => TimestampBinary(1, t.some)
      case Some((LogAppendTime, t)) => TimestampBinary(2, t.some)
      case Some((UnknownTime, t)) => TimestampBinary(3, t.some)
      case _ => TimestampBinary(-1, None)
    }

  }
}
