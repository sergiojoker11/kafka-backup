package com.sj11.kafka.backup.service

import cats.effect.Async
import cats.implicits._
import com.sj11.kafka.backup.service.RecordConverter.toBinary
import com.sj11.kafka.backup.service.Utils.backedUpRecord
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath
import fs2.kafka.ConsumerRecord

import java.nio.file.Path

trait Backup[F[_]] {
  def apply(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): F[Unit]

}
class FileSystemBackup[F[_]: Async](backupPath: Path) {

  def backup(record: ConsumerRecord[Array[Byte], Array[Byte]]): F[Unit] = for {
    r <- Async[F].fromTry(toBinary(record))
    recordPath = fromNioPath(backedUpRecord(backupPath, r.topic, r.partition, r.offset))
    _ <- Files[F].createDirectories(recordPath.parent.orNull)
    _ <- Files[F].createFile(recordPath)
    _ <- fs2.Stream.emits(r.content).through(Files[F].writeAll(recordPath)).compile.drain
  } yield ()

}
