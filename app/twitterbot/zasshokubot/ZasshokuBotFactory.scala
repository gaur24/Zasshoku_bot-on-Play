package twitterbot.zasshokubot

import java.util.Properties
import java.io.FileInputStream
import java.nio.file.Paths
import java.io.FileOutputStream
import twitterbot.api.BotFactory
import twitterbot.api.TwitterAPI
import java.util.Timer
import java.util.Date
import models.ZasshokuParameters
import models.ZasshokuParameters
import models.ZasshokuParameters

object ZasshokuBotFactory {

  private val usersFileName = "users.dat"
  private val propFileName = "bot.properties"

  private val isEnableTweetKey = "isEnableTweet"
  private val delayTweetKey = "delayTweet"
  private val periodTweetKey = "periodTweet"

  private val isEnableReplyKey = "isEnableReply"
  private val delayReplyKey = "delayReply"
  private val periodReplyKey = "periodReply"
  private val replyCountKey = "replyCount"
  private val replyLimitKey = "replyLimit"

  private val zassyokuIDKey = "zassyokuID"
  private val zassyokuRatioKey = "zassyokuRatio"
  private val responseTimeKey = "responseTime"

  def createZasshokuBot(botName: String): ZasshokuBot = {
    val twitterAPI = BotFactory.createTwitterAPI(botName)
    val usersPath = Paths.get(botName, "users.dat")
    val userManager = new ZasshokuUserManager(usersPath)
    new ZasshokuBot(botName, twitterAPI, userManager)
  }
  
  def createZasshokuParameters(botName: String): ZasshokuParameters = {
    val propFile = Paths.get(botName, propFileName).toFile
    val prop = new Properties
    prop.load(new FileInputStream(propFile))

    val isEnableTweet = prop.getProperty(isEnableTweetKey).toBoolean
    val delayTweet = prop.getProperty(delayTweetKey).toInt
    val periodTweet = prop.getProperty(periodTweetKey).toInt

    val isEnableReply = prop.getProperty(isEnableReplyKey).toBoolean
    val delayReply = prop.getProperty(delayReplyKey).toInt
    val periodReply = prop.getProperty(periodReplyKey).toInt
    val replyCount = prop.getProperty(replyCountKey).toInt
    val replyLimit = prop.getProperty(replyLimitKey).toInt

    val zassyokuID = prop.getProperty(zassyokuIDKey).toLong
    val zassyokuRatio = prop.getProperty(zassyokuRatioKey).toInt
    val responseTime = prop.getProperty(responseTimeKey).toInt
    
    new ZasshokuParameters(
        isEnableTweet, delayTweet, periodTweet, 
        isEnableReply, delayReply, periodReply, 
        replyCount, replyLimit, zassyokuRatio, zassyokuID, responseTime)
  }

  def updateProperties(botName: String, para: ZasshokuParameters) = {
    
    val propFile = Paths.get(botName, propFileName).toFile
    val prop = new Properties
    prop.load(new FileInputStream(propFile))

    prop.setProperty(isEnableTweetKey, para.isEnableTweet.toString)
    prop.setProperty(delayTweetKey, para.delayTweet.toString)
    prop.setProperty(periodTweetKey, para.periodTweet.toString)

    prop.setProperty(isEnableReplyKey, para.isEnableReply.toString)
    prop.setProperty(delayReplyKey, para.delayReply.toString)
    prop.setProperty(periodReplyKey, para.periodReply.toString)
    prop.setProperty(replyCountKey, para.replyCount.toString)
    prop.setProperty(replyLimitKey, para.replyLimit.toString)

    prop.setProperty(zassyokuRatioKey, para.zassyokuRatio.toString)
    prop.setProperty(zassyokuIDKey, para.zassyokuID.toString)
    prop.setProperty(responseTimeKey, para.responseTime.toString)

    prop.store(new FileOutputStream(propFile), "")
  }

  def createNewTimer(botName: String, twitterAPI: TwitterAPI, userManager: ZasshokuUserManager): Timer = {
    val propFile = Paths.get(botName, propFileName).toFile
    val prop = new Properties
    prop.load(new FileInputStream(propFile))
    
    val toMilliSecond = 1000L;

    val isEnableTweet = prop.getProperty(isEnableTweetKey).toBoolean
    val delayTweet = prop.getProperty(delayTweetKey).toInt * toMilliSecond
    val periodTweet = prop.getProperty(periodTweetKey).toInt * toMilliSecond

    val isEnableReply = prop.getProperty(isEnableReplyKey).toBoolean
    val delayReply = prop.getProperty(delayReplyKey).toInt * toMilliSecond
    val periodReply = prop.getProperty(periodReplyKey).toInt * toMilliSecond
    val replyCount = prop.getProperty(replyCountKey).toInt
    val replyLimit = prop.getProperty(replyLimitKey).toInt

    val zassyokuID = prop.getProperty(zassyokuIDKey).toLong
    val zassyokuRatio = prop.getProperty(zassyokuRatioKey).toInt
    val responseTime = prop.getProperty(responseTimeKey).toInt * toMilliSecond

    var timer = new Timer
    if (isEnableTweet) {
      val tweetTask = new ZasshokuTweet(twitterAPI, zassyokuID, zassyokuRatio, responseTime)
      timer.schedule(tweetTask, delayTweet, periodTweet)
    }
    if (isEnableReply) {
      val replyTask = new ZasshokuReply(twitterAPI, zassyokuID, zassyokuRatio, responseTime, replyCount, replyLimit, userManager)
      timer.schedule(replyTask, delayReply, periodReply)
    }
    timer
  }
}