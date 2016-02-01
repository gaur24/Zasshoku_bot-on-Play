package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.slf4j.LoggerFactory

import play.api._
import play.api.mvc._
import twitterbot.animeprogressbot.HTMLParser
import twitterbot.api.MyLogger
import twitterbot.zasshokubot.ZasshokuBot

class zasshoku_bot extends Controller {

  private val logger = LoggerFactory.getLogger(getClass)

  def index = Action {
    Ok(views.html.index(""))
  }
  
  def start = Action {
    ZasshokuBot.start()
        logger.info("bot started")
    val logs = MyLogger.list().toList
    Ok(views.html.log("bot running", logs))
  }

  def stop = Action {
    ZasshokuBot.stop()
    logger.info("bot stoped")
    val logs = MyLogger.list().toList
    Ok(views.html.log("bot stoped", logs))
  }

  def zasshokuRanking = Action {
    Ok(views.html.zasshokuRank("Zasshoku Ranking", ZasshokuBot.users.values.toList))
  }

  def log = Action {
    val logs = MyLogger.list().toList
    Ok(views.html.log("bot", logs))
  }

}
