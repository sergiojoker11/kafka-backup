package com.sj11.kafka.backup

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.sj11.kafka.backup.kafka.Consumer
import com.sj11.kafka.backup.service.config.{BackupConfig, Filesystem}
import com.sj11.kafka.backup.service.FileSystemBackup
import org.typelevel.log4cats.slf4j.Slf4jLogger

object BackupMain extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Backup")
      _ <- logger.info(s"Using config ${BackupConfig.get}")
      _ <- backupResources(BackupConfig.get).use { consumer =>
        consumer.stream.compile.drain.start.flatMap(_.joinWithUnit)
      }
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }

  private def backupResources(config: BackupConfig): Resource[IO, Consumer[IO]] = {
    for {
      consumer <- Consumer.create[IO](
        config.consumer,
        new FileSystemBackup[IO](config.service.asInstanceOf[Filesystem].backupPath).backup
      )
    } yield consumer
  }
}
