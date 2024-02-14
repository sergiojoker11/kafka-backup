package com.sj11.kafka.backup

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.sj11.kafka.backup.kafka.Producer
import com.sj11.kafka.backup.service.config.{Filesystem, RestoreConfig}
import com.sj11.kafka.backup.service.FileSystemRestore
import com.sj11.kafka.backup.utils.FileBackupIterator.streamMessages
import org.typelevel.log4cats.slf4j.Slf4jLogger

object RestoreMain extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Restore")
      _ <- logger.info(s"Using config ${RestoreConfig.get}")
      _ <- restoreResources(RestoreConfig.get).use { restoreService =>
        streamMessages[IO](RestoreConfig.get.service.asInstanceOf[Filesystem].backupPath).evalMap {
          case (topic, partition, offset, _) =>
            restoreService.restore(topic, partition, offset)
        }.compile.drain
      }
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }

  private def restoreResources(config: RestoreConfig): Resource[IO, FileSystemRestore[IO]] = {
    for {
      producer <- Producer.create[IO](
        config.producer
      )
    } yield new FileSystemRestore[IO](config.service.asInstanceOf[Filesystem].backupPath, producer)
  }
}
