package com.sj11.kafka.backup.service.config

import com.typesafe.config.Config

import java.nio.file.{Path, Paths}

sealed trait ServiceConfig

case class Filesystem(backupPath: Path) extends ServiceConfig

object ServiceConfig {

  def apply(config: Config): ServiceConfig =
    config.getString("implementation") match {
      case "filesystem" => {
        val filesystemConfig = config.getConfig("filesystem")
        Filesystem(Paths.get(filesystemConfig.getString("backup-path")))
      }
      case other =>
        throw new IllegalArgumentException(s"Service implementation with name $other does not exist. Exiting...")
    }
}
