package twitterbot.zasshokubot

import twitterbot.api.TwitterAPI
import twitterbot.api.timertask.AbstractTwitterTask
import twitterbot.api.timertask.PeriodicReplyTask
import twitter4j.Status
import twitterbot.zasshokubot.markov.MarkovController
import scala.collection.JavaConversions._

class ZasshokuReply(twitterAPI: TwitterAPI, mentionCount: Int, replyLimit: Int, _zassyokuID: Long, _zassyokuRaio: Int)
    extends PeriodicReplyTask(twitterAPI, mentionCount, replyLimit) {
  
  private val zasshokuID = _zassyokuID
  private val zassyokuRatio = _zassyokuRaio
  
  private val expReply = 1
  
  override def makeReply(mention: Status): Option[String] = {
    val user = mention.getUser
    
    if(user.getScreenName == twitterAPI.screenName){
      return None
    }
    
    var homeTimeline = twitterAPI.homeTimeline(200).toList
    if(zassyokuRatio > 0){
      homeTimeline = List.concat(homeTimeline, twitterAPI.userHomeTimeline(zasshokuID, zassyokuRatio * 4).toList)
    }
    
    var reply = MarkovController.generateSentence(homeTimeline, twitterAPI.screenName)
    
    // 文字数が140文字を超える場合、zasshoku_botが伝えきれないことを表現
		// [@screenName reply]
    val limit = TwitterAPI.tweetLengthMax - 3 - user.getScreenName.size - 2
    if(reply.isDefined && reply.size > limit){
      reply = Some(reply.get.slice(0, limit).toString() + "文字数")
    }
    reply
  }

  override def postProcessingOfSuccess(mention: Status) = {
    
    var user = if(ZasshokuBot.users.contains(mention.getUser.getId)){
      val existUser = ZasshokuBot.users.get(mention.getUser.getId).get
      existUser.screenName = mention.getUser.getScreenName
      existUser.isProtected = mention.getUser.isProtected
      existUser
    } else {
      val newUser = new ZasshokuUser(mention.getUser.getId, mention.getUser.getScreenName, mention.getUser.isProtected)
      ZasshokuBot.users.put(mention.getUser.getId, newUser)
      newUser
    }

    val level = user.gainExp(expReply)
    if(level._2 > level._1){
      val reply = "@" + user.screenName + " " + "レベルアップ！" + level._1 + "→" + level._2 + " 次のレベルまであと" + user.nextLevelUpExp + "exp"
    }

  }
}