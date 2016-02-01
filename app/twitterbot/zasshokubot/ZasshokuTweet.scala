package twitterbot.zasshokubot

import twitterbot.api.timertask.PeriodicTweetTask
import twitterbot.api.TwitterAPI
import twitterbot.zasshokubot.markov.MarkovController
import scala.collection.JavaConversions._


class ZasshokuTweet(twitterAPI: TwitterAPI, _zassyokuID: Long, _zassyokuRatio: Int) extends PeriodicTweetTask(twitterAPI) {
 
  private val zassyokuRatio = _zassyokuRatio
  private val zassyokuID = _zassyokuID
  
  override def makeTweet(): Option[String] = {
        var homeTimeline = twitterAPI.homeTimeline(200).toList
    if(zassyokuRatio > 0){
      homeTimeline = List.concat(homeTimeline, twitterAPI.userHomeTimeline(zassyokuID, zassyokuRatio * 4).toList)
    }
    var tweet = MarkovController.generateSentence(homeTimeline.seq, twitterAPI.screenName)
    
    // 文字数が140文字を超える場合、zasshoku_botが伝えきれないことを表現
    val limit = TwitterAPI.tweetLengthMax - 3
    if(tweet.isDefined && tweet.size > limit){
      tweet = Some(tweet.get.slice(0, limit).toString() + "文字数")
    }
    tweet
  }
}