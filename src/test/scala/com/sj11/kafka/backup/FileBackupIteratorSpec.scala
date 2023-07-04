package com.sj11.kafka.backup

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.sj11.kafka.backup.Utils.backup
import com.sj11.kafka.backup.service.Utils.backedUpRecord
import com.sj11.kafka.backup.utils.FileBackupIterator
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.nio.file.Path
import scala.collection.immutable.HashMap

class FileBackupIteratorSpec extends AnyFlatSpec {

  "FileBackupIteratorSpec" should "create a representation of messages so they can be read" in {
    val backupPath = backup(this.toString())
    val (topic, partition, offset) = ("topic-1", 6, 45265L)
    val recordPath = backedUpRecord(backupPath, topic, partition, offset)
    createTestFiles(recordPath).unsafeRunSync()
    val result = FileBackupIterator
      .readMessagesInOrder[IO](backupPath)
      .unsafeRunSync()
    val expectation = HashMap(topic -> HashMap(partition -> List((topic, partition, offset, recordPath))))
    result.toString() shouldEqual expectation.toString()
  }

  it should "be ordered" in {
    val backupPath = backup(this.toString())
    val (topic1, partition1, offset1) = ("topic-1", 6, 2L)
    val (topic2, partition2, offset2) = ("topic-1", 6, 1L)
    val (topic3, partition3, offset3) = ("topic-2", 6, 2L)
    val (topic4, partition4, offset4) = ("topic-2", 6, 1L)
    val (topic5, partition5, offset5) = ("topic-1", 2, 5L)
    val (topic6, partition6, offset6) = ("topic-1", 2, 6L)
    val recordPath1 = backedUpRecord(backupPath, topic1, partition1, offset1)
    val recordPath2 = backedUpRecord(backupPath, topic2, partition2, offset2)
    val recordPath3 = backedUpRecord(backupPath, topic3, partition3, offset3)
    val recordPath4 = backedUpRecord(backupPath, topic4, partition4, offset4)
    val recordPath5 = backedUpRecord(backupPath, topic5, partition5, offset5)
    val recordPath6 = backedUpRecord(backupPath, topic6, partition6, offset6)
    List(recordPath1, recordPath2, recordPath3, recordPath4, recordPath5, recordPath6).map(p =>
      createTestFiles(p).unsafeRunSync())
    val result = FileBackupIterator
      .readMessagesInOrder[IO](backupPath)
      .unsafeRunSync()
    val expectation = HashMap(
      "topic-1" -> HashMap(
        6 -> List(("topic-1", 6, 1L, recordPath2), ("topic-1", 6, 2L, recordPath1)),
        2 -> List(("topic-1", 2, 5L, recordPath5), ("topic-1", 2, 6L, recordPath6))
      ),
      "topic-2" -> HashMap(
        6 -> List(("topic-2", 6, 1L, recordPath4), ("topic-2", 6, 2L, recordPath3))
      )
    )
    result.toString() shouldEqual expectation.toString()
  }

  def createTestFiles(recordPath: Path) = {
    val path = fromNioPath(recordPath)
    for {
      _ <- Files[IO].createDirectories(path.parent.orNull)
      _ <- Files[IO].createFile(path)
    } yield ()
  }
}
