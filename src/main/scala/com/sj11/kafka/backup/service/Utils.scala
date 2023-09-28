package com.sj11.kafka.backup.service

import cats.implicits._
import cats.effect.Async
import com.sj11.kafka.backup.service.model.RecordBackup
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath

import java.nio.file.Path

object Utils {
  def backedUpRecord(backupPath: Path, topic: String, partition: Int, offset: Long): Path =
    backupPath.resolve(topic).resolve(partition.toString).resolve(offset.toString)

  def writeRecordInDisk[F[_]: Async: Files](backupPath: Path, r: RecordBackup): F[Unit] = {
    val recordPath = fromNioPath(backedUpRecord(backupPath, r.topic, r.partition, r.offset))
    for {
      _ <- Files[F].createDirectories(recordPath.parent.orNull)
      _ <- Files[F].createFile(recordPath)
      _ <- fs2.Stream.emits(r.content).through(Files[F].writeAll(recordPath)).compile.drain
    } yield ()
  }
}
