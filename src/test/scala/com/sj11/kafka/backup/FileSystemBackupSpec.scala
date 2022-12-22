package com.sj11.kafka.backup

import cats.effect.IO
import com.sj11.kafka.backup.Utils.{assertR, testConsumerRecord}
import com.sj11.kafka.backup.service.FileSystemBackup
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.file.{Path, Paths}
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.service.RecordConverter.fromBinary
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath

class FileSystemBackupSpec extends AnyFlatSpec {

  it should "ok" in {
    val (topic, partition) = ("test-topic", 12)
    val inputRecord = testConsumerRecord(topic, partition)
    val backupPath = Paths.get("/tmp", this.toString())
    val recordPath = Paths.get(backupPath.toString, topic, partition.toString, inputRecord.offset.toString)
    (for {
      _ <- service(backupPath).backup(inputRecord)
      record <- Files[IO].readAll(fromNioPath(recordPath)).compile.toList.map(_.toArray)
      consumerRecord <- IO.fromTry(fromBinary(topic, partition, record))
    } yield assertR(inputRecord, consumerRecord)).unsafeRunSync()
  }

  def service(backupPath: Path) = new FileSystemBackup[IO](backupPath)
}
