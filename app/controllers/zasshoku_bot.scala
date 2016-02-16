package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.ZasshokuParameters
import twitterbot.animeprogressbot.HTMLParser
import twitterbot.api.MyLogger
import twitterbot.zasshokubot.ZasshokuBot
import twitterbot.zasshokubot.ZasshokuBotFactory
import javax.inject.Inject
import play.api.i18n.{ I18nSupport, MessagesApi, Messages, Lang }

class zasshoku_bot @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  private val botName = "zasshoku_bot"
  private val zasshokuBot = ZasshokuBotFactory.createZasshokuBot(botName)
  private val logger = LoggerFactory.getLogger(getClass)

  // 1年が31536000秒なので8桁まで
  val zasshokuForm = Form(
    mapping(
      "isEnableTweet" -> boolean,
      "delayTweet" -> number(min = 0, max = 99999999),
      "periodTweet" -> number(min = 0, max = 99999999),
      "isEnableReply" -> boolean,
      "delayReply" -> number(min = 0, max = 99999999),
      "periodReply" -> number(min = 0, max = 99999999),
      "replyLimit" -> number(min = 0, max = 999),
      "replyCount" -> number(min = 0, max = 999),
      "zassyokuRatio" -> number(min = 0, max = 50),
      "zassyokuID" -> longNumber(min = 0, max = Long.MaxValue),
      "responseTime" -> number(min = 1, max = 99)
    )(ZasshokuParameters.apply)(ZasshokuParameters.unapply))

  def index = Action {
    Ok(views.html.index(""))
  }

  def start = Action {
    zasshokuBot.start()
    Redirect(routes.zasshoku_bot.setting)
  }

  def stop = Action {
    zasshokuBot.stop()
    Redirect(routes.zasshoku_bot.setting)
  }

  def zasshokuRanking = Action {
    Ok(views.html.zasshokuRank(botName, zasshokuBot.users.sortBy(_.totalExp).reverse))
  }

  def log = Action {
    Ok(views.html.log(botName, MyLogger.logs))
  }

  def setting = Action {
    val parameters = ZasshokuBotFactory.createZasshokuParameters(botName)
    Ok(views.html.setting(botName)(zasshokuForm.fill(parameters))(zasshokuBot.state))
  }

  def set = Action { implicit request =>
    {
      val parameters = ZasshokuBotFactory.createZasshokuParameters(botName)
      zasshokuForm.bindFromRequest.fold(
        hasErrors => {
          logger.info("setting error: " + hasErrors.errors.toString)
          BadRequest(views.html.setting(botName)(hasErrors)(zasshokuBot.state))
        },
        success => {
          ZasshokuBotFactory.updateProperties(botName, success)
          logger.info("setting success!")
          zasshokuBot.stop()
          Redirect(routes.zasshoku_bot.setting)

        })
    }
  }

}
