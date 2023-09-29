package com.sj11.kafka.backup

import cats.effect.IO
import com.sj11.kafka.backup.Utils.{assertR, backup}
import com.sj11.kafka.backup.service.FileSystemBackup
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.file.Path
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.service.Utils.backedUpRecord
import com.sj11.kafka.backup.service.model.RecordBackup
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath

class FileSystemBackupSpec extends AnyFlatSpec {

  it should "back up a record" in {
    val (topic, partition, offset, content) = ("test-topic", 12, 1L, "someContent".getBytes)
    val inputRecord = RecordBackup(topic, partition, offset, content)
    val backupPath = backup(this.toString())
    val recordPath = backedUpRecord(backupPath, topic, partition, inputRecord.offset)
    (for {
      _ <- service(backupPath).backup(inputRecord)
      content <- Files[IO].readAll(fromNioPath(recordPath)).compile.toList.map(_.toArray)
      resultRecord = RecordBackup(topic, partition, inputRecord.offset, content)
    } yield assertR(inputRecord, resultRecord)).unsafeRunSync()
  }

  def service(backupPath: Path) = new FileSystemBackup[IO](backupPath)
}
