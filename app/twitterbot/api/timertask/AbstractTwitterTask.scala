package twitterbot.api.timertask

import java.util.TimerTask
import twitter4j.TwitterException
import org.slf4j.LoggerFactory
import twitterbot.api.TwitterAPI
import twitterbot.api.TwitterError
import twitterbot.api.MyLogger

abstract class AbstractTwitterTask(_twitterAPI: TwitterAPI) extends TimerTask {

  val twitterAPI = _twitterAPI

  private val logger = LoggerFactory.getLogger(getClass)

  def periodic()

  override def run() = {
    try {
      periodic()
    } catch {
      case e: TwitterException =>
        if (e.isCausedByNetworkIssue()) {
          logger.error(MyLogger.reduceMessage("isCausedByNetworkIssue", e))
        } else {
          
          e.getErrorCode match {
            case TwitterError.duplicateStatus =>
              logger.error(MyLogger.reduceMessage("duplicateStatus", e))
            case TwitterError.couldNotFollowByAlreadyRequested =>
              logger.error(MyLogger.reduceMessage("couldNotFollowByAlreadyRequested", e))
            case TwitterError.statusOver140Characters =>
              logger.error(MyLogger.reduceMessage("statusOver140Characters", e))
            case TwitterError.rateLimitExceeded =>
              logger.error(MyLogger.reduceMessage("rateLimitExceeded", e))
            case _ =>
              logger.error(MyLogger.reduceMessage(e))
          }
        }
      case t: Throwable =>
        logger.error(MyLogger.reduceMessage(t))
    }
  }
}
