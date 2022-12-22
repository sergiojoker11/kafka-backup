package com.sj11.kafka.backup

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.Utils.{assertR, testConsumerRecord}
import com.sj11.kafka.backup.service.RecordConverter
import org.scalatest.flatspec.AnyFlatSpec

class RecordConverterSpec extends AnyFlatSpec {

  it should "ok" in {
    val (topic, partition) = ("test-topic", 12)
    val inputRecord = testConsumerRecord(topic, partition)
    (for {
      b <- IO.fromTry(RecordConverter.toBinary(inputRecord))
      resultingRecord <- IO.fromTry(RecordConverter.fromBinary(topic, partition, b.content))
    } yield assertR(resultingRecord, inputRecord)).unsafeRunSync()

  }
}
