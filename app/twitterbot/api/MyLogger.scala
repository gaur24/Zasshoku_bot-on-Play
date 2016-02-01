package twitterbot.api

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._



object MyLogger {
  private val logPath = Paths.get("logs", "application.log")

  def list(): Seq[String] = {
    Files.readAllLines(logPath).seq
  }
}