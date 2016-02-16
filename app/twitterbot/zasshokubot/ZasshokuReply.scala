package twitterbot.zasshokubot

import scala.collection.JavaConversions.asScalaBuffer
import twitter4j.Status
import twitterbot.api.TwitterAPI
import twitterbot.api.timertask.PeriodicReplyTask
import twitterbot.zasshokubot.markov.MarkovController
import java.nio.file.Path
import java.util.Random

class ZasshokuReply(twitterAPI: TwitterAPI, _zassyokuID: Long, _zassyokuRatio: Int, _responseTime: Long, replyCount: Int, replyLimit: Int, _userManager: ZasshokuUserManager)
    extends PeriodicReplyTask(twitterAPI, replyCount, replyLimit) {

  private val zasshokuID = _zassyokuID
  private val zassyokuRatio = _zassyokuRatio
  private val responseTime = _responseTime
  private val userManager = _userManager

  private val random = new Random
  private val expReply = 1

  override def makeReply(mention: Status): Option[String] = {
    val user = mention.getUser

    if (user.getScreenName == twitterAPI.screenName) {
      return None
    }

    // 鍵付きユーザーのツイートは拾わない
    var homeTimeline = twitterAPI.homeTimeline(200).toSeq.filterNot(_.getUser.isProtected)
    
    if (zassyokuRatio > 0) {
      homeTimeline = List.concat(homeTimeline, twitterAPI.userHomeTimeline(zasshokuID, zassyokuRatio * 4).toList)
    }

    // 大体の文字数をランダムに決める
    val sentenceLimit = random.nextInt(TwitterAPI.tweetLengthMax)
    
    var reply = MarkovController.generateSentence(homeTimeline, twitterAPI.screenName, sentenceLimit, responseTime)

    // 文字数が140文字を超える場合、zasshoku_botが伝えきれないことを表現
    // [@screenName reply]
    val limit = TwitterAPI.tweetLengthMax - 3 - user.getScreenName.size - 2
    if (reply.isDefined && reply.get.length > limit) {
      reply = Some(reply.get.slice(0, limit).toString() + "文字数")
    }
    reply
  }

  /**
   * リプライに成功した後ユーザーの経験値を更新する
   */
  override def postProcessingOfSuccess(mention: Status) = {
    
    val optionUser = userManager.find(mention.getUser.getId)

    var user = if (optionUser.isDefined) {
      var existUser = optionUser.get
      existUser.screenName = mention.getUser.getScreenName
      existUser.isProtected = mention.getUser.isProtected
      existUser
    } else {
      new ZasshokuUser(mention.getUser.getId, mention.getUser.getScreenName, mention.getUser.isProtected)
    }

    val level = user.gainExp(expReply)
    if (level._1 < level._2) {
      val reply = "@" + mention.getUser.getScreenName + " " + "レベルアップ！" + level._1 + "→" + level._2 + " 次のレベルまであと" + user.nextLevelUpExp + "exp"
      twitterAPI.postReply(reply, mention.getId)
    }
        
    userManager.update(user)

  }
}