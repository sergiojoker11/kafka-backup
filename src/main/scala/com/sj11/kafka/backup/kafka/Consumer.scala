package com.sj11.kafka.backup.kafka

import cats.effect._
import cats.implicits._
import com.sj11.kafka.backup.kafka.model.{ConsumerConfig, Ssl}
import com.sj11.kafka.backup.utils.LoggingUtil.withDebugLogging
import fs2.kafka.{
  commitBatchWithin,
  AdminClientSettings,
  ConsumerRecord,
  ConsumerSettings,
  Deserializer,
  KafkaAdminClient,
  KafkaConsumer
}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._
import java.util.UUID

trait Consumer[F[_]] {
  def stream: fs2.Stream[F, Unit]
}

class ConsumerImpl[F[_]](
  config: ConsumerConfig,
  task: ConsumerRecord[Array[Byte], Array[Byte]] => F[Unit],
  adminClient: KafkaAdminClient[F],
  consumer: KafkaConsumer[F, Array[Byte], Array[Byte]]
)(implicit F: Async[F])
  extends Consumer[F] {

  implicit val logger = Slf4jLogger.getLogger[F]
  println(adminClient)
  def stream = {
    fs2
      .Stream(consumer)
      .evalTap(_.subscribe(config.topicRegex.r))
      .flatMap(_.stream)
      .evalMap { committable =>
        withDebugLogging(
          s"Consuming record. Topic: ${committable.record.topic}. Partition: ${committable.record.partition} with offset: ${committable.record.offset}") {
          task(committable.record)
            .map(_ => committable.offset)
        }
      }
      .onError { case e =>
        fs2.Stream.eval(logger.error(
          s"Error parsing kafka event with message: '${e.getMessage}'. ${e.getStackTrace.toList.mkString("\n")} "))
      }
      .through(commitBatchWithin(1, 15.seconds))

  }

}

object Consumer {

  def create[F[_]: Async](
    config: ConsumerConfig,
    task: ConsumerRecord[Array[Byte], Array[Byte]] => F[Unit]): Resource[F, Consumer[F]] = {

    val adminClientSettings = AdminClientSettings(config.kafkaBootstrap)
      .withClientId(s"backup-service-${UUID.randomUUID()}")
      .withProperties(Ssl(config))
    val adminClientResourceRef: Resource[F, KafkaAdminClient[F]] = KafkaAdminClient.resource(adminClientSettings)
    val consumerSettings =
      ConsumerSettings[F, Array[Byte], Array[Byte]](
        keyDeserializer = Deserializer.identity,
        valueDeserializer = Deserializer.identity)
        .withBootstrapServers(config.kafkaBootstrap)
        .withGroupId(config.groupId)
        .withClientId(config.clientId)
        .withAutoOffsetReset(config.autoOffsetReset)
        .withProperties(Ssl(config))
    val consumerResourceRef: Resource[F, KafkaConsumer[F, Array[Byte], Array[Byte]]] =
      KafkaConsumer.resource(consumerSettings)

    for {
      adminClient <- adminClientResourceRef
      consumer <- consumerResourceRef
    } yield new ConsumerImpl[F](
      config,
      task,
      adminClient,
      consumer
    )
  }
}
