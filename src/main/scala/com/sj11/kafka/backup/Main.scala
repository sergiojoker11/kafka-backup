package com.sj11.kafka.backup

import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info(s"Starting Kafka Backup")
      _ <- logger.info("Exiting")
    } yield ExitCode.Success).onError(err =>
      logger.error(s"Execution stopped unexpectedly. Error: ${err.getLocalizedMessage}"))
  }
}
