package twitterbot.api.timertask

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import twitter4j.Status
import twitter4j.TwitterException
import twitterbot.api.TwitterAPI
import twitterbot.api.TwitterError

abstract class PeriodicReplyTask(twitterAPI: TwitterAPI, _mentionCount: Int, _replyLimit: Int) extends AbstractTwitterTask(twitterAPI) {

  private val mentionCount = _mentionCount
  private val replyLimit = _replyLimit
  private var isDuplicate = false
  private var replyCountMap = HashMap[Long, Int]()

  def makeReply(mention: Status): Option[String]

  def postProcessingOfSuccess(mention: Status)

  override def periodic = {
    var removeUserIDs = HashSet[Long]()
    var updateUserIDs = HashSet[Long]()

    // ややこしいことするのでcatchしてまた投げます
    try {

      // statusIDの古い順に並べる
      val mentions = twitterAPI.mentions(mentionCount)
        .filterNot { x => x.getUser.getScreenName == twitterAPI.screenName }
        .reverse

      for (mention <- mentions) {

        val replyCount = replyCountMap.getOrElse(mention.getUser.getId, 0)

        // リプライ連続回数の上限を超えてなければ
        if (replyCount < replyLimit) {
          val createdReply = this.makeReply(mention)
          // makeReplyされたら返事をする
          if (createdReply.isDefined) {
            val reply = "@" + mention.getUser.getScreenName + " " + createdReply.get
            twitterAPI.postReply(reply, mention.getId, isDuplicate)
            
            postProcessingOfSuccess(mention)
            
            isDuplicate = false
            removeUserIDs.remove(mention.getUser.getId)
            updateUserIDs += mention.getUser.getId
            replyCountMap.put(mention.getUser.getId, replyCount + 1)
          }

          // 上限を超えたまたはmakeReplyされないので返事をしない
        } else {
          twitterAPI.doNotReplyToThisTweet(mention.getId)
          removeUserIDs += mention.getUser.getId
        }
      }
    } catch {
      case e: TwitterException =>
        if (e.getErrorCode == TwitterError.duplicateStatus) {
          isDuplicate = true
          throw new TwitterException(e)
        } else {
          throw new TwitterException(e)
        }
      case e: Exception =>
        throw new Exception(e)
      case e: Throwable =>
        throw new Throwable(e)
    } finally {
      // 必ず最後に更新処理
      removeUserIDs.foreach { x => replyCountMap.remove(x) }
      for (id <- replyCountMap.keySet) {
        if (!updateUserIDs.contains(id)) {
          replyCountMap.remove(id)
        }
      }
    }

  }
}