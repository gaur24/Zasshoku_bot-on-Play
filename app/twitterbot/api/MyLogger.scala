package twitterbot.api

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.text.ParsePosition

object MyLogger {
  private val logPath = Paths.get("logs", "application.log")

  /**
   * ログを読み込みます
   */
  def logs(): Seq[String] = {
    Files.readAllLines(logPath).seq
  }

  def reduceMessage(message: String, t: Throwable): String = {
    val stackTrace = t.getStackTrace
    message + System.lineSeparator + stackTrace.map(_.toString)
      .reduceLeft(_ + System.lineSeparator + _)
  }
  
  def reduceMessage(t: Throwable): String = {
    MyLogger.reduceMessage(t.getMessage, t)
  }
}
