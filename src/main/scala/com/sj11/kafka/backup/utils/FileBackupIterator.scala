package com.sj11.kafka.backup.utils

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps, toTraverseOps}
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath

import java.io.File
import java.nio.file.Path
import java.nio.file.{Files => JFiles}
import scala.util.Try

object FileBackupIterator {

  def streamMessages[F[_]: Async](path: Path): fs2.Stream[F, (String, Int, Long, fs2.io.file.Path)] = Files[F]
    .walk(fromNioPath(path))
    .filter(p => JFiles.isRegularFile(p.toNioPath))
    .evalMap(p =>
      Async[F]
        .fromTry(Try {
          val segments = p.toString.split(File.separator).toList
          (segments.init.init.last, segments.init.last.toInt, segments.last.toLong, p)
        }))

  def readMessagesInOrder[F[_]: Async](
    path: Path): F[Map[String, Map[Int, List[(String, Int, Long, fs2.io.file.Path)]]]] = {
    Files[F]
      .walk(fromNioPath(path))
      .compile
      .toList
      .map(_.filter(p => JFiles.isRegularFile(p.toNioPath)))
      .flatMap(_.map(p =>
        Async[F].fromTry(Try {
          val segments = p.toString.split(File.separator).toList
          (segments.init.init.last, segments.init.last.toInt, segments.last.toLong, p)
        })).sequence.map(_.groupBy(_._1).map { case (topic, messages) =>
        topic -> messages.groupBy(_._2).map { case (partition, messages) =>
          (partition, messages.sortBy(_._3))
        }
      }))
  }

}
