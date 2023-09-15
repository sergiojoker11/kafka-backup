package com.sj11.kafka.backup

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.Utils.{assertR, backup}
import com.sj11.kafka.backup.service.{FileSystemRestore, RecordBinary, TimestampBinary}
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.file.Path
import com.sj11.kafka.backup.service.Utils.writeRecordInDisk
import com.sj11.kafka.backup.service.model.RecordBackup
import fs2.kafka.Timestamp

class FileSystemRestoreSpec extends AnyFlatSpec {

  it should "restore a record" in {
    val (topic, partition, offset) = ("test-topic", 12, 345L)
    val backupPath = backup(this.toString())
    val recordBinary = RecordBinary(
      offset,
      TimestampBinary.from(Timestamp.logAppendTime(56755656L)),
      "someKey".getBytes,
      "someValue".getBytes,
      List())
    val inputRecordBackup = RecordBackup(topic, partition, offset, recordBinary.toByteArray)
    (for {
      _ <- writeRecordInDisk[IO](backupPath, inputRecordBackup)
      _ <- service(backupPath).restore(topic, partition, offset)
      resultRecordBackup: RecordBackup = null // comething coming from producer result or something
    } yield assertR(inputRecordBackup, resultRecordBackup)).unsafeRunSync()
  }

  def service(backupPath: Path) = new FileSystemRestore[IO](backupPath, ???)
}
