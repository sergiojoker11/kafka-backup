package com.sj11.kafka.backup

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.Utils.backup
import com.sj11.kafka.backup.kafka.Producer
import com.sj11.kafka.backup.service.{FileSystemRestore, RecordBinary, TimestampBinary}
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.file.Path
import com.sj11.kafka.backup.service.Utils.writeRecordInDisk
import com.sj11.kafka.backup.service.model.{RecordBackup, RecordRestoreResult}
import fs2.kafka.Timestamp
import org.scalamock.scalatest.MockFactory

class FileSystemRestoreSpec extends AnyFlatSpec with MockFactory {

  it should "restore a record" in {
    val (topic, partition, offset) = ("test-topic", 12, 345L)
    val backupPath = backup(this.toString())
    val producer = mock[Producer[IO]]
    val recordBinary = RecordBinary(
      offset,
      TimestampBinary.from(Timestamp.logAppendTime(56755656L)),
      "someKey".getBytes,
      "someValue".getBytes,
      List())
    val inputRecordBackup = RecordBackup(topic, partition, offset, recordBinary.toByteArray)
    (producer
      .produces(_))
      .expects(*)
      .returning(IO.pure(List(RecordRestoreResult(topic, partition, offset, recordBinary.timestamp.getIfDefined.get))))
      .once()
    (for {
      _ <- writeRecordInDisk[IO](backupPath, inputRecordBackup)
      r <- service(backupPath, producer).restore(topic, partition, offset)
    } yield r)
      .unsafeRunSync()
  }

  def service(backupPath: Path, producer: Producer[IO]) = new FileSystemRestore[IO](backupPath, producer)
}
