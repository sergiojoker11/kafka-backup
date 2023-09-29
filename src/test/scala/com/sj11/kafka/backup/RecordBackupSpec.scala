package com.sj11.kafka.backup

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.Utils.{assertR, testConsumerRecord}
import com.sj11.kafka.backup.service.model.RecordBackup
import org.scalatest.flatspec.AnyFlatSpec

// TODO: this test needs re-thinking as it no longer makes sense as it is written
class RecordBackupSpec extends AnyFlatSpec {

  it should "convert a consumer record into a backup record" in {
    val (topic, partition) = ("test-topic", 12)
    val inputRecord = testConsumerRecord(topic, partition)
    (for {
      inputRecordBackup <- IO.fromTry(RecordBackup.from(inputRecord))
      resultRecordBackup <- IO.fromTry(RecordBackup.from(inputRecord))
    } yield assertR(inputRecordBackup, resultRecordBackup)).unsafeRunSync()

  }
}
