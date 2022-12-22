package com.sj11.kafka.backup.utils

import cats.FlatMap
import cats.effect.Clock
import cats.implicits._
import org.typelevel.log4cats.SelfAwareStructuredLogger

import scala.language.implicitConversions

/** Provides aspect-orientated logging methods. Note that logger arguments are lazily evaluated, meaning they are only
  * evaluated if the log level matches.
  *
  * Example:
  *
  * withDebugLogging("Foo") { ... foo.bar ... }
  *
  * Would log something like this:
  *
  * Started: [Foo].
  *
  * Done : [Foo] Result: [computation_result]. In [elapsed_time] milliseconds
  */
object LoggingUtil {

  /** Wrapper to allow passing arguments by name to the logger.
    *
    * Unfortunately, Scala 2 doesn't allow passing repeated parameters by name, so we need a wrapper with an implicit
    * conversion to overcome this limitation.
    *
    * The benefit of this is that arguments passed to `withXXXLogging` methods will only be evaluated if needed, and not
    * on the method call.
    */
  class LogArg(v: => Any) {
    def value: Any = v
  }

  object LogArg {
    implicit def fromAny(a: => Any): LogArg = new LogArg(a)
  }

  def withDebugLogging[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.debug(_))(items)(f)
  }

  def withDebugLogging[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.debug(mdcContext)(_))(items)(f)
  }

  def withDebugLoggingNoResult[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.debug(_))(items)(f)
  }

  def withDebugLoggingNoResult[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(
    implicit logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.debug(mdcContext)(_))(items)(f)
  }

  def withErrorLogging[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.error(_))(items)(f)
  }

  def withErrorLogging[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.error(mdcContext)(_))(items)(f)
  }

  def withErrorLoggingNoResult[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.error(_))(items)(f)
  }

  def withErrorLoggingNoResult[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(
    implicit logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.error(mdcContext)(_))(items)(f)
  }

  def withInfoLogging[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.info(_))(items)(f)
  }

  def withInfoLogging[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.info(mdcContext)(_))(items)(f)
  }

  def withInfoLoggingNoResult[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.info(_))(items)(f)
  }

  def withInfoLoggingNoResult[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(
    implicit logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.info(mdcContext)(_))(items)(f)
  }

  def withTraceLogging[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.trace(_))(items)(f)
  }

  def withTraceLogging[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.trace(mdcContext)(_))(items)(f)
  }

  def withTraceLoggingNoResult[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.trace(_))(items)(f)
  }

  def withTraceLoggingNoResult[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(
    implicit logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.trace(mdcContext)(_))(items)(f)
  }

  def withWarnLogging[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.warn(_))(items)(f)
  }

  def withWarnLogging[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = true, logger.warn(mdcContext)(_))(items)(f)
  }

  def withWarnLoggingNoResult[R, F[_]: FlatMap: Clock](items: LogArg*)(f: F[R])(implicit
    logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.warn(_))(items)(f)
  }

  def withWarnLoggingNoResult[R, F[_]: FlatMap: Clock](mdcContext: Map[String, String], items: LogArg*)(f: F[R])(
    implicit logger: SelfAwareStructuredLogger[F]): F[R] = {
    withLogging(includeResult = false, logger.warn(mdcContext)(_))(items)(f)
  }

  private def withLogging[R, F[_]: FlatMap: Clock](includeResult: Boolean, log: (=> String) => F[Unit])(
    items: Seq[LogArg])(f: => F[R]): F[R] = {
    lazy val itemsFormatted = items.map(_.value).mkString(", ")
    for {
      _ <- log(s"Started: [$itemsFormatted].")
      start <- implicitly[Clock[F]].monotonic
      res <- f
      end <- implicitly[Clock[F]].monotonic
      _ <-
        if (includeResult)
          log(s"Done: [$itemsFormatted] Result: [$res]. In ${(end - start).toMillis} milliseconds")
        else
          log(s"Done: [$itemsFormatted]. In ${(end - start).toMillis} milliseconds")
    } yield res
  }
}
