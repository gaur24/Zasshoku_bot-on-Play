package twitterbot.api.timertask

import java.util.TimerTask
import twitter4j.TwitterException
import org.slf4j.LoggerFactory
import twitterbot.api.TwitterAPI
import twitterbot.api.TwitterError

abstract class AbstractTwitterTask(_twitterAPI: TwitterAPI) extends TimerTask {

  val twitterAPI = _twitterAPI

  private val logger = LoggerFactory.getLogger(getClass)

  def periodic()

  def reduceMessage(message: String, stackTrace: Array[StackTraceElement]): String = {
    message + System.lineSeparator + stackTrace.map(_.toString)
      .reduceLeft(_ + System.lineSeparator + _)
  }

  override def run() = {
    try {
      periodic()
    } catch {
      case e: TwitterException =>
        if (e.isCausedByNetworkIssue()) {
          logger.error(reduceMessage("isCausedByNetworkIssue", e.getStackTrace))
        } else {
          
          e.getErrorCode match {
            case TwitterError.duplicateStatus =>
              logger.error(reduceMessage("duplicateStatus", e.getStackTrace))
            case TwitterError.couldNotFollowByAlreadyRequested =>
              logger.error(reduceMessage("couldNotFollowByAlreadyRequested", e.getStackTrace))
            case TwitterError.statusOver140Characters =>
              logger.error(reduceMessage("statusOver140Characters", e.getStackTrace))
            case _ =>
              logger.error(reduceMessage(e.getMessage, e.getStackTrace))
          }
        }
      case e: Exception =>
        logger.error(reduceMessage(e.getMessage, e.getStackTrace))
      case e: Throwable =>
        logger.error(reduceMessage(e.getMessage, e.getStackTrace))
    }
  }
}
