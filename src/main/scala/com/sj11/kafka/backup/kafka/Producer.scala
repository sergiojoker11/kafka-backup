package com.sj11.kafka.backup.kafka

import cats.effect._
import cats.implicits._
import com.sj11.kafka.backup.kafka.model.{ProducerConfig, Ssl}
import com.sj11.kafka.backup.service.model.{RecordRestore, RecordRestoreResult}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.kafka
import fs2.kafka._

trait Producer[F[_]] {
  def produces(records: List[RecordRestore]): F[List[RecordRestoreResult]]
}

class ProducerImp[F[_]: Sync](producer: kafka.KafkaProducer[F, Array[Byte], Array[Byte]]) extends Producer[F] {

  implicit val logger = Slf4jLogger.getLogger[F]

  def produces(records: List[RecordRestore]): F[List[RecordRestoreResult]] =
    records.map { record =>
      producer.produce(ProducerRecords.one(record.to)).map(_.map(_.map(r => RecordRestoreResult.from(r._2))))
    }.sequence
      .flatMap(_.sequence)
      .map(_.flatMap(_.toList))
      .onError(err =>
        logger
          .error(
            s"There was an error publishing a batch of events to kafka. Error message: ${err.getLocalizedMessage}. Error: $err"))
}

object Producer {
  def create[F[_]: Async](producerConfig: ProducerConfig): Resource[F, Producer[F]] = {
    val producerSettings = ProducerSettings[F, Array[Byte], Array[Byte]](
      keySerializer = Serializer.identity,
      valueSerializer = Serializer.identity
    ).withBootstrapServers(producerConfig.kafkaBootstrap)
      .withEnableIdempotence(true)
      .withRetries(producerConfig.noRetries)
      .withClientId(producerConfig.clientId)
      .withProperties(Ssl(producerConfig))

    KafkaProducer[F]
      .resource[Array[Byte], Array[Byte]](producerSettings)
      .map(new ProducerImp[F](_))
  }
}
