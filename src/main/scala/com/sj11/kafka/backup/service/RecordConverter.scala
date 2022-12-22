package com.sj11.kafka.backup.service

import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxOptionId}
import fs2.kafka.HeaderSerializer.identity
import fs2.kafka.{ConsumerRecord, Header, Headers, Timestamp}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import scala.util.Try

object RecordConverter {
  def toBinary(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): Try[RecordBackup] = Try {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val dataStream = new DataOutputStream(byteArrayOutputStream)
    val binary = RecordBinary.from(consumerRecord)
    dataStream.writeLong(binary.offset)
    dataStream.writeInt(binary.timestamp.`type`)
    binary.timestamp.value.map(dataStream.writeLong)
    dataStream.writeInt(binary.key.length)
    dataStream.write(binary.key)
    dataStream.writeInt(binary.value.length)
    dataStream.write(binary.value)
    dataStream.writeInt(binary.headers.length)
    binary.headers.map(h => {
      dataStream.writeInt(h.key.length)
      dataStream.write(h.key)
      dataStream.writeInt(h.value.length)
      dataStream.write(h.value)
    })
    dataStream.flush()
    RecordBackup(
      consumerRecord.topic,
      consumerRecord.partition,
      consumerRecord.offset,
      byteArrayOutputStream.toByteArray)
  }.onError { case e =>
    Try(println(s"toBinary has exploded. Error: ${e.getLocalizedMessage}"))
  }

  def fromBinary(topic: String, partition: Int, record: Array[Byte]): Try[ConsumerRecord[Array[Byte], Array[Byte]]] =
    Try {
      val byteArrayOutputStream = new ByteArrayInputStream(record)
      val dataStream = new DataInputStream(byteArrayOutputStream)
      val offset = dataStream.readLong()
      val timestampType = TimestampType(dataStream.readInt())
      val timestamp = timestampType match {
        case NoTime => None
        case _ => Timestamp.createTime(dataStream.readLong()).some
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

      val r = ConsumerRecord
        .apply(topic = topic, partition = partition, offset = offset, key = key, value = value)
        .withHeaders(Headers.fromSeq(headers))
      timestamp.fold(r)(t => r.withTimestamp(t))
    }

  // format: off
/**
 * # Record Format:
 *   offset: int64
 *   timestampType: int32 -2 if timestamp is null
 *   [timestamp: int64] if timestampType != NO_TIMESTAMP_TYPE && timestamp != null
 *   keyLength: int32
 *   [key: byte[keyLength]] if keyLength >= 0
 *   valueLength: int32
 *   [value: byte[valueLength]] if valueLength >= 0
 *   headerCount: int32
 *   headers: Header[headerCount]
 *
 *  # Header Format:
 *    headerKeyLength: int32
 *    headerKey: byte[headerKeyLength]
 *    headerValueLength: int32
 *    [headerValue:byte[headerValueLength]] if headerValueLength >= 0
 **/
  // format: on

}
