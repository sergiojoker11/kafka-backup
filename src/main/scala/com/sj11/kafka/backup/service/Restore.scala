package com.sj11.kafka.backup.service

import cats.effect.Async
import cats.implicits._
import com.sj11.kafka.backup.kafka.Producer
import com.sj11.kafka.backup.service.Utils.backedUpRecord
import com.sj11.kafka.backup.service.model.{RecordBackup, RecordRestore}
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath
import fs2.kafka.{Headers, ProducerRecord}

import java.nio.file.Path

trait Restore[F[_]] {
  def apply(topic: String, partition: Int, record: Array[Byte]): F[Unit]

}
class FileSystemRestore[F[_]: Async: Files](backupPath: Path, producer: Producer[F]) {

  def restore(topic: String, partition: Int, offset: Long): F[Unit] = {
    val recordPath = fromNioPath(backedUpRecord(backupPath, topic, partition, offset))
    for {
      record <- Files[F].readAll(recordPath).compile.toList.map(_.toArray)
      r <- Async[F].fromTry(RecordRestore.from(RecordBackup(topic, partition, offset, record)))
      _ <- producer
        .produces(
          List(
            ProducerRecord(r.topic, r.binary.key, r.binary.value)
              .withPartition(r.partition)
              .withHeaders(Headers.fromIterable(r.binary.headers.map(_.to())))))
    } yield ()
  }

}
