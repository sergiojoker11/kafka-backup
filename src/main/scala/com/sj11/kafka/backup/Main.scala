package com.sj11.kafka.backup

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.sj11.kafka.backup.kafka.Consumer
import com.sj11.kafka.backup.kafka.model.ConsumerConfig
import com.sj11.kafka.backup.service.Backup
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Backup")
      _ <- resources().use { consumer =>
        consumer.stream.compile.drain.start.flatMap(_.joinWithUnit)
      }
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }

  private def resources(): Resource[IO, Consumer[IO]] = {
    for {
      consumer <- Consumer.create[IO](
        ConsumerConfig.get,
        new Backup[IO].apply
      )
    } yield consumer
  }
}
