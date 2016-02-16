package twitterbot.api

import twitter4j.conf.ConfigurationBuilder
import twitter4j.TwitterFactory
import twitter4j.Twitter
import java.util.Properties
import java.io.FileOutputStream
import java.nio.file.Paths
import java.io.FileInputStream
import java.nio.file.Files
import java.util.Arrays

object BotFactory {
  val tokenFileName = "token.properties"
  val twitterDirectoryName = "TwitterData"
  val notFollowUsersFileName = "notFollowUsers.dat"
  val notUnfollowUsersFileName = "notUnfollowUsers.dat"
  val lastReplyIDFileName = "lastReplyID.dat"
  val lastHomeTimelineFileName = "lastHomeTimeline.dat"

  /**
   * 既存ファイルを読み込んでTwitterAPIオブジェクトを作成
   */
  def createTwitterAPI(botName: String): TwitterAPI = {
    // パスの取得
    val notFollowUsers = Paths.get(botName, twitterDirectoryName, notFollowUsersFileName)
    val notUnfollowUsers = Paths.get(botName, twitterDirectoryName, notUnfollowUsersFileName)
    val lastReplyID = Paths.get(botName, twitterDirectoryName, lastReplyIDFileName)
    val lastHomeTimeline = Paths.get(botName, twitterDirectoryName, lastHomeTimelineFileName)

    val tokenProperty = new Properties
    tokenProperty.load(new FileInputStream(Paths.get(botName, tokenFileName).toFile()))

    val updator = new TwitterUpdator(notFollowUsers, notUnfollowUsers, lastReplyID, lastHomeTimeline)
    val twitter = this.createTwitter(tokenProperty)
    new TwitterAPI(twitter, updator)
  }

  /**
   * トークンを元に新規にファイルを生成してTwitterAPIオブジェクトを生成
   */
  def makeNewTwitterAPI(
    botName: String,
    consumerKey: String,
    consumerSecret: String,
    accessToken: String,
    accessTokenSecret: String): TwitterAPI = {

    // パスの取得
    val notFollowUsers = Paths.get(botName, twitterDirectoryName, notFollowUsersFileName)
    val notUnfollowUsers = Paths.get(botName, twitterDirectoryName, notUnfollowUsersFileName)
    val lastReplyID = Paths.get(botName, twitterDirectoryName, lastReplyIDFileName)
    val lastHomeTimeline = Paths.get(botName, twitterDirectoryName, lastHomeTimelineFileName)

    // ファイル生成
    Paths.get(botName, twitterDirectoryName).toFile().mkdirs()
    notFollowUsers.toFile().createNewFile()
    notUnfollowUsers.toFile().createNewFile()
    Files.write(lastReplyID, Arrays.asList("0"))
    Files.write(lastHomeTimeline, Arrays.asList("0"))

    // プロパティファイル生成
    val tokenProperty = makeNewToken(botName, consumerKey, consumerSecret, accessToken, accessTokenSecret)

    val updator = new TwitterUpdator(notFollowUsers, notUnfollowUsers, lastReplyID, lastHomeTimeline)
    val twitter = this.createTwitter(tokenProperty)
    new TwitterAPI(twitter, updator)
  }

  private def makeNewToken(
    botName: String,
    consumerKey: String,
    consumerSecret: String,
    accessToken: String,
    accessTokenSecret: String): Properties = {

    val tokenPath = Paths.get(botName, tokenFileName)
    val tokenProperty = new Properties
    tokenProperty.setProperty("consumerKey", consumerKey)
    tokenProperty.setProperty("consumerSecret", consumerSecret)
    tokenProperty.setProperty("accessToken", accessToken)
    tokenProperty.setProperty("accessTokenSecret", accessTokenSecret)

    val fos = new FileOutputStream(tokenPath.toFile())
    tokenProperty.store(fos, botName + "'s token")
    tokenProperty
  }

  private def createTwitter(token: Properties): Twitter = {
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true)
    cb.setOAuthConsumerKey(token.getProperty("consumerKey"))
    cb.setOAuthConsumerSecret(token.getProperty("consumerSecret"))
    cb.setOAuthAccessToken(token.getProperty("accessToken"))
    cb.setOAuthAccessTokenSecret(token.getProperty("accessTokenSecret"))
    new TwitterFactory(cb.build()).getInstance
  }
}