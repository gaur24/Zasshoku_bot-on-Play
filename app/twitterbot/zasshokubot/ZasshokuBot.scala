package twitterbot.zasshokubot

import java.util.Timer
import twitterbot.api.BotFactory
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import scala.collection.JavaConverters._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object ZasshokuBot {
  private var isRunning = false
  private var botName = "zasshoku_bot"
  private val zassyokuID = 175335014L
  val usersPath = Paths.get(botName, "users.dat")
  private var timer = new Timer
  private var twitterAPI = BotFactory.createTwitterAPI(botName)

  var users = HashMap[Long, ZasshokuUser]()

  def stop() {
    if (isRunning) {
      timer.cancel()
      isRunning = false
    }
  }

  def start() {
    if (!isRunning) {
      timer.cancel()
      timer = new Timer
      timer.schedule(new ZasshokuTweet(twitterAPI, zassyokuID, 0), 1000, 100000)
      timer.schedule(new ZasshokuReply(twitterAPI, 5, 3, zassyokuID, 0), 30000, 100000)
      isRunning = true
      println("start " + botName)
    }
  }

  def writeZasshokuUsers() = {
    val jsons = users.values.map { x => Json.toJson(x).toString() }.toList
    Files.write(usersPath, jsons.asJava)
  }

  def readZasshokuUsers() = {
    Files.readAllLines(usersPath).asScala
      .map { x => Json.parse(x).validate[ZasshokuUser] }.toList
  }
}

