package com.sj11.kafka.backup.service

import cats.implicits._
import fs2.kafka.{ConsumerRecord, Header, Timestamp}

import java.io.{ByteArrayOutputStream, DataOutputStream}

case class RecordBinary(
  offset: Long,
  timestamp: TimestampBinary,
  key: Array[Byte],
  value: Array[Byte],
  headers: List[HeaderRecordBinary]) {
  def toByteArray: Array[Byte] = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val dataStream = new DataOutputStream(byteArrayOutputStream)
    dataStream.writeLong(offset)
    dataStream.writeInt(timestamp.`type`)
    timestamp.value.map(dataStream.writeLong)
    dataStream.writeInt(key.length)
    dataStream.write(key)
    dataStream.writeInt(value.length)
    dataStream.write(value)
    dataStream.writeInt(headers.length)
    headers.map(h => {
      dataStream.writeInt(h.key.length)
      dataStream.write(h.key)
      dataStream.writeInt(h.value.length)
      dataStream.write(h.value)
    })
    dataStream.flush()
    byteArrayOutputStream.toByteArray
  }
}

object RecordBinary {
  def from(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): RecordBinary = {
    RecordBinary(
      offset = consumerRecord.offset,
      timestamp = TimestampBinary.from(consumerRecord.timestamp),
      key = consumerRecord.key,
      value = consumerRecord.value,
      headers = consumerRecord.headers.toChain.map(HeaderRecordBinary.from(_)).toList
    )
  }
}

case class HeaderRecordBinary(key: Array[Byte], value: Array[Byte]) {
  def to(): Header = Header.apply(new String(key), value)
}
object HeaderRecordBinary {
  def from(h: Header) = HeaderRecordBinary(h.key().getBytes, h.value())
}
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

case class TimestampBinary(`type`: Int, value: Option[Long]) {
  def getIfDefined: Option[Long] = if (`type` > 0) value else None
}

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
