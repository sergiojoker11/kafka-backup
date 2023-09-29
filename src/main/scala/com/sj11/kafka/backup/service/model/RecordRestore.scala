package com.sj11.kafka.backup.service.model

import cats.implicits._
import com.sj11.kafka.backup.service.{
  CreateTime,
  HeaderRecordBinary,
  LogAppendTime,
  NoTime,
  RecordBinary,
  TimestampBinary,
  TimestampType,
  UnknownTime
}
import fs2.kafka.{Header, Headers, ProducerRecord, Timestamp}

import java.io.{ByteArrayInputStream, DataInputStream}
import scala.util.Try

case class RecordRestore(topic: String, partition: Int, offset: Long, binary: RecordBinary) {
  def to: ProducerRecord[Array[Byte], Array[Byte]] = {
    val r = ProducerRecord(topic, binary.key, binary.value)
      .withPartition(partition)
      .withHeaders(Headers.fromIterable(binary.headers.map(_.to())))
    binary.timestamp.getIfDefined.fold(r)(r.withTimestamp(_))
  }
}

object RecordRestore {
  def from(record: RecordBackup): Try[RecordRestore] =
    Try {
      val byteArrayOutputStream = new ByteArrayInputStream(record.content)
      val dataStream = new DataInputStream(byteArrayOutputStream)
      val offset = dataStream.readLong()
      val timestampTypeVal = dataStream.readInt()
      val timestampType = TimestampType(timestampTypeVal)
      val timestamp = timestampType match {
        case NoTime => None
        case CreateTime => Timestamp.createTime(dataStream.readLong()).some
        case UnknownTime => Timestamp.unknownTime(dataStream.readLong()).some
        case LogAppendTime => Timestamp.logAppendTime(dataStream.readLong()).some
      }
      val keyLength = dataStream.readInt()
      val key = dataStream.readNBytes(keyLength)
      val valueLength = dataStream.readInt()
      val value = dataStream.readNBytes(valueLength)
      val headerCount = dataStream.readInt()
      val headers = List
        .fill(headerCount)(headerCount)
        .map(_ => {
          val headerKeyLength = dataStream.readInt()
          val headerKey = dataStream.readNBytes(headerKeyLength)
          val headerValueLength = dataStream.readInt()
          val headerValue = dataStream.readNBytes(headerValueLength)
          Header(new String(headerKey), headerValue)
        })

      val b = RecordBinary(
        offset = offset,
        timestamp = TimestampBinary(
          timestampTypeVal,
          timestampType match {
            case CreateTime => timestamp.flatMap(_.createTime)
            case LogAppendTime => timestamp.flatMap(_.logAppendTime)
            case UnknownTime => timestamp.flatMap(_.unknownTime)
            case NoTime => None
            case _ => throw new Exception("Booom! Some mess about timestamps")
          }
        ),
        key = key,
        value = value,
        headers = headers.map(HeaderRecordBinary.from(_))
      )
      assert(record.offset == offset)
      RecordRestore
        .apply(topic = record.topic, partition = record.partition, offset = record.offset, binary = b)
    }
}
