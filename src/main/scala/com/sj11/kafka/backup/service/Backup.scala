package com.sj11.kafka.backup.service

import cats.effect.Async
import cats.implicits._
import com.sj11.kafka.backup.service.Utils.writeRecordInDisk
import com.sj11.kafka.backup.service.model.RecordBackup
import fs2.io.file.Files
import fs2.kafka.ConsumerRecord

import java.nio.file.Path

trait Backup[F[_]] {
  def apply(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): F[Unit]

}
class FileSystemBackup[F[_]: Async: Files](backupPath: Path) {

  def backup(record: ConsumerRecord[Array[Byte], Array[Byte]]): F[Unit] = for {
    recordBackup <- Async[F].fromTry(RecordBackup.from(record))
    _ <- writeRecordInDisk[F](backupPath, recordBackup)
  } yield ()

}
