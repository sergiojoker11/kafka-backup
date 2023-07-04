package com.sj11.kafka.backup.service

import java.nio.file.Path

object Utils {
  def backedUpRecord(backupPath: Path, topic: String, partition: Int, offset: Long): Path =
    backupPath.resolve(topic).resolve(partition.toString).resolve(offset.toString)
}
