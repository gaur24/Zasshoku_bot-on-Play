package twitterbot.api.timertask

import twitterbot.api.TwitterAPI

abstract class PeriodicTweetTask(twitterAPI: TwitterAPI) extends AbstractTwitterTask(twitterAPI) {

  def makeTweet(): Option[String]

  override def periodic() = {
    val tweet = makeTweet()
    if (tweet.isDefined) twitterAPI.postTweet(tweet.get)
  }
}