package twitterbot.zasshokubot.markov

import scala.collection.JavaConversions._
import twitter4j.Status
import twitterbot.api.TwitterAPI

object MarkovController {
  private val regexMention = "@\\w+\\W"
  private val regexRT = "RT[ ]@\\w+:[ ]"
  private val regexHashTag = "#\\S+[　\\s]"

  def generateSentence(timeline: Seq[Status], myScreenName: String): Option[String] = {
    val textList = timeline
      .filterNot { x => x.getText.contains("http") }
      .filterNot { x => x.getUser.getScreenName == myScreenName }
      .map { x =>
        x.getText.replaceAll(regexRT, "")
          .replaceAll(regexHashTag, "")
          .replaceAll(regexMention, "")
      }
      .map { x => x.split("#").head }
      .filterNot { x => x.isEmpty }
      .toList
    
    List.range(0, 10).par.map { x => 
      this.generateSentenceByThread(textList, 3, TwitterAPI.tweetLengthMax, 5000)
      }
      .filterNot { x => x == null }
      .filterNot { x => x.isEmpty() }
      .headOption
  }

  private def generateSentenceByThread(textList: Seq[String], rank: Int, sentenceLimit: Int, waitTime: Long): String = {
    val thread = new MarkovThread(textList, rank, sentenceLimit)
    thread.start()
    Thread.sleep(waitTime)
    if (thread.getState != Thread.State.TERMINATED) {
      thread.stopThread()
      return null
    }
    thread.getResult
  }
}