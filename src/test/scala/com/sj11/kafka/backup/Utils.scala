package com.sj11.kafka.backup

import fs2.kafka.{ConsumerRecord, Header, Headers, Timestamp}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.nio.file.Paths
import java.time.Instant

object Utils {

  def testConsumerRecord(topic: String, partition: Int): ConsumerRecord[Array[Byte], Array[Byte]] = {
    val (offset, key, value, timestamp) =
      (123L, "someKey", "someValue", Instant.now.toEpochMilli)
    val headers = Headers.fromSeq(List(Header("someHeaderKey", "someHeaderValue".getBytes)))
    ConsumerRecord(topic, partition, offset, key.getBytes, value.getBytes)
      .withHeaders(headers)
      .withTimestamp(Timestamp.createTime(timestamp))
  }

  def assertR(r1: ConsumerRecord[Array[Byte], Array[Byte]], r2: ConsumerRecord[Array[Byte], Array[Byte]]): Assertion = {
    r1.topic shouldEqual r2.topic
    r1.partition shouldEqual r2.partition
    r1.offset shouldEqual r2.offset
    r1.key shouldEqual r2.key
    r1.value shouldEqual r2.value
    r1.headers.toChain.toList.zip(r2.headers.toChain.toList).foreach { case (h1, h2) =>
      h1.key() shouldEqual h2.key()
      h1.value() shouldEqual h2.value()
    }
    r1.timestamp.createTime shouldEqual r2.timestamp.createTime
  }

  def backup(name: String) = Paths.get("/tmp", name, Instant.now().toEpochMilli.toString)
}
