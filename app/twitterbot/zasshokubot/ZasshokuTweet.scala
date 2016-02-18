package twitterbot.zasshokubot

import scala.collection.JavaConversions.asScalaBuffer
import twitterbot.api.TwitterAPI
import twitterbot.api.timertask.PeriodicTweetTask
import twitterbot.zasshokubot.markov.MarkovController
import java.util.Random

class ZasshokuTweet(twitterAPI: TwitterAPI, _zassyokuID: Long, _zassyokuRatio: Int, _responseTime: Long) extends PeriodicTweetTask(twitterAPI) {

  private val zassyokuRatio = _zassyokuRatio
  private val zassyokuID = _zassyokuID
  private val responseTime = _responseTime
  private val random = new Random

  override def makeTweet(): Option[String] = {
    // 鍵付きユーザーのツイートは拾わない
    var homeTimeline = twitterAPI.homeTimeline(200).toSeq.filterNot(_.getUser.isProtected).toList
    if (zassyokuRatio > 0) {
      homeTimeline = List.concat(homeTimeline, twitterAPI.userHomeTimeline(zassyokuID, zassyokuRatio * 4).toList)
    }
    
    // 大体の文字数をランダムに決める
    // あまり長いのはつぶやかないような確率分布
    val sentenceLimit = random.nextInt(random.nextInt(TwitterAPI.tweetLengthMax - 1)) + 1
    
    var tweet = MarkovController.generateSentence(homeTimeline.seq, twitterAPI.screenName, sentenceLimit, responseTime)

    // 文字数が140文字を超える場合、zasshoku_botが伝えきれないことを表現
    val limit = TwitterAPI.tweetLengthMax - 3
    if (tweet.isDefined && tweet.get.length > limit) {
      tweet = Some(tweet.get.slice(0, limit) + "文字数")
    }
    tweet
  }
}