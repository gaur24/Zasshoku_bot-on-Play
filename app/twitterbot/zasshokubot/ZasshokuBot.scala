package twitterbot.zasshokubot

import java.util.Timer
import org.slf4j.LoggerFactory
import twitterbot.api.TwitterAPI
import twitterbot.api.MyLogger

class ZasshokuBot(_botName: String, _twitterAPI: TwitterAPI, _userManager: ZasshokuUserManager) {
  val botName = _botName
  private val twitterAPI = _twitterAPI
  private val userManager = _userManager

  private var isRunning = false
  private var timer = new Timer
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * botの稼働状態を通知
   */
  def state(): Boolean = {
    isRunning
  }

  /**
   * botを停止します
   */
  def stop(): Unit = {
    if (isRunning) {
      timer.cancel()
      timer.purge()
      timer = null
      isRunning = false
      logger.info("stop " + botName)
    }
  }

  /**
   * botを開始します
   */
  def start(): Unit = {
    try {
      if (!isRunning) {
        timer.cancel()
        timer.purge()
        timer = null
        timer = ZasshokuBotFactory.createNewTimer(botName, twitterAPI, userManager)
        isRunning = true
        logger.info("start " + botName)
      }
    } catch {
      case t: Throwable =>
        timer.cancel()
        timer.purge()
        timer = null
        isRunning = false
        logger.error(MyLogger.reduceMessage(t))
    }
  }

  def users(): Seq[ZasshokuUser] = {
    userManager.getUsers
  }
}



