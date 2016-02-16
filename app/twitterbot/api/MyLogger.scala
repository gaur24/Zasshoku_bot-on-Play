package twitterbot.api

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.text.ParsePosition

object MyLogger {
  private val logPath1 = Paths.get("logs", "application.log.1")
  private val logPath2 = Paths.get("logs", "application.log.2")
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private val oldTime = "1970-01-01 01:00:00"

  /**
   * ログを読み込みます
   */
  def logs(): Seq[String] = {
    // ログが2つあるので新しい方を
    val log1 = Files.readAllLines(logPath1).seq
    val log2 = Files.readAllLines(logPath2).seq
    val head1 = sdf.parse(log1.headOption.getOrElse(oldTime).take(oldTime.size)).getTime
    val head2 = sdf.parse(log2.headOption.getOrElse(oldTime).take(oldTime.size)).getTime
    
    if(head1 > head2){
      log1
    } else {
      log2
    }
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
