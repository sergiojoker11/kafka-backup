package com.sj11.kafka.backup

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.sj11.kafka.backup.kafka.{Consumer, Producer}
import com.sj11.kafka.backup.kafka.model.{ConsumerConfig, ProducerConfig}
import com.sj11.kafka.backup.service.{FileSystemBackup, FileSystemRestore}
import com.sj11.kafka.backup.utils.FileBackupIterator.streamMessages
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.file.Paths

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Backup")
      _ <- backupResources().use { consumer =>
        consumer.stream.compile.drain.start.flatMap(_.joinWithUnit)
      }
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }

  private def backupResources(): Resource[IO, Consumer[IO]] = {
    for {
      consumer <- Consumer.create[IO](
        ConsumerConfig.get,
        new FileSystemBackup[IO](Paths.get("/tmp")).backup
      )
    } yield consumer
  }

  def runRestore(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Restore")
      _ <- restoreResources().use { restoreService =>
        streamMessages[IO](Paths.get("/tmp")).evalMap { case (topic, partition, offset, _) =>
          restoreService.restore(topic, partition, offset)
        }.compile.drain
      }
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }

  private def restoreResources(): Resource[IO, FileSystemRestore[IO]] = {
    for {
      producer <- Producer.create[IO](
        ProducerConfig.get
      )
    } yield new FileSystemRestore[IO](Paths.get("/tmp"), producer)
  }
}
