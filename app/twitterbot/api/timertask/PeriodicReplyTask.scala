package twitterbot.api.timertask

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import twitter4j.Status
import twitter4j.TwitterException
import twitterbot.api.TwitterAPI
import twitterbot.api.TwitterError

abstract class PeriodicReplyTask(twitterAPI: TwitterAPI, _replyCount: Int, _replyLimit: Int) extends AbstractTwitterTask(twitterAPI) {

  private val replyCount = _replyCount
  private val replyLimit = _replyLimit
  private val replyCountMap = HashMap[Long, Int]()

  def makeReply(mention: Status): Option[String]

  def postProcessingOfSuccess(mention: Status)

  override def periodic = {

    val limitedUserIDs = replyCountMap.filter(p => p._2 >= replyLimit).keySet
    val updateUserIDs = HashSet[Long]()

    // statusIDの古い順に並べる
    val mentions = twitterAPI.mentions(replyCount)
      .filterNot(_.getUser.getScreenName == twitterAPI.screenName)
      .reverse

    for (mention <- mentions) {

      val optionReply = this.makeReply(mention).map("@" + mention.getUser.getScreenName + " " + _)

      // リプライが生成されないまたは上限に達したユーザーは返事をしない
      if (optionReply.isEmpty || limitedUserIDs.contains(mention.getUser.getId)) {
        println("not reply -> " + mention.getText)
        twitterAPI.postReply(None, mention.getId, false)
      } else {
        println("reply -> " + mention.getText)
        twitterAPI.postReply(optionReply, mention.getId, false)

        postProcessingOfSuccess(mention)

        // 更新
        replyCountMap.put(mention.getUser.getId, replyCountMap.get(mention.getUser.getId).getOrElse(0) + 1)

        // 更新したかどうかを覚えておく
        updateUserIDs.add(mention.getUser.getId)
      }

    }

    // 回数を増やしたユーザーは連続して返事をしているので上限チェック対象
    replyCountMap.retain((k, v) => updateUserIDs.contains(k))

  }
}