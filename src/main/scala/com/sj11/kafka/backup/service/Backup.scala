package com.sj11.kafka.backup.service

import cats.effect.Sync
import fs2.kafka.ConsumerRecord

class Backup[F[_]: Sync] {

  def apply(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): F[Unit] =
    Sync[F].delay(println("Backed up record: " + consumerRecord.toString))
}
