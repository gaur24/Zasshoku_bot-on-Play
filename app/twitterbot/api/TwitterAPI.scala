package twitterbot.api

import scala.collection.JavaConversions.asScalaBuffer
import scala.util.Random

import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.User

/**
 * Twitterクラスのラッパークラス
 */
class TwitterAPI(_twitter: Twitter, _updator: TwitterUpdator) {
  private val twitter = _twitter
  private val updator = _updator
  private val random = new Random
  var screenName = twitter.getScreenName
  initAPI()

  private def initAPI() = {
    val latestStatusID = twitter.getHomeTimeline(new Paging(1, 1)).get(0).getId
    updator.updateLastReplyID(latestStatusID)
    updator.updateLastHomeTimelineID(latestStatusID)
  }

  /**
   * ユーザーIDからユーザー情報を取得
   */
  def showUser(userID: Long): User = {
    twitter.showUser(userID)
  }

  /**
   * フォロワーのIDを取得<br>
   * 約5000件まで取得可能
   */
  def followersIDs(): Seq[Long] = {
    var cursor = -1L
    wrapLongArray(twitter.getFollowersIDs(cursor).getIDs).toList
  }

  /**
   * フォローしているユーザーのIDを取得<br>
   * 約5000件まで取得可能
   */
  def friendsIDs(): Seq[Long] = {
    var cursor = -1L
    wrapLongArray(twitter.getFriendsIDs(cursor).getIDs).toList
  }

  /**
   * フォローされたらフォローを返す<br>
   * notCreateFriendshipIDsで指定されたユーザーはフォローしない<br>
   * 既にフォロー申請済みの非公開ユーザーに対してフォローしようとした場合、<br>
   * そのユーザーのIDをnotCreateFriendshipIDsに追加し、ファイル書き込みする
   */
  def followBack() = {
    val friendsIDs = this.friendsIDs()
    val newFollowers = this.followersIDs()
      .filterNot { x => friendsIDs.contains(x) }
      .filterNot { x => updator.notFollowUserIDs.contains(x) }
      .toList

    newFollowers.foreach { id =>
      try {
        twitter.createFriendship(id)
      } catch {
        case e: TwitterException =>
          if (e.getErrorCode == TwitterError.couldNotFollowByAlreadyRequested) {
            updator.addNotFollowUser(id)
          } else {
            throw new TwitterException(e)
          }
      }
    }
  }

  /**
   * フォローを外されたら自分もフォローを外す<br>
   * notDestroyFriendshipIDsで指定されたユーザーのフォローは外さない
   */
  def unfollowNotFollowMe() = {
    val followersIDs = this.followersIDs()
    val notFollowMeUsers = this.friendsIDs()
      .filterNot { x => followersIDs.contains(x) }
      .filterNot { x => updator.notUnfollowUserIDs.contains(x) }
      .toList

    notFollowMeUsers.foreach { id => twitter.destroyFriendship(id) }
  }

  /**
   * ホームタイムラインから最新count件のツイートを取得
   */
  def homeTimeline(count: Int): ResponseList[Status] = {
    twitter.getHomeTimeline(new Paging(1, count))
  }

  /**
   * ホームタイムラインから最新count件のツイートを取得<br>
   * このメソッドで取得した最新のツイートIDは保持され、再度タイムラインを取得する場合、前回からの更新分だけを返す
   */
  def homeTimelineDifference(count: Int): ResponseList[Status] = {
    val homeTimeline = twitter.getHomeTimeline(new Paging(1, count, updator.lastHomeTimelineID))
    // TODO どっちか
    //    updator.updateLastHomeTimelineID(homeTimeline.head.getId)
    val id = if (homeTimeline.isEmpty()) 0L else homeTimeline.last.getId
    updator.updateLastHomeTimelineID(id)
    return homeTimeline
  }

  /**
   * 特定のユーザーのタイムラインから最新count件のツイートを取得
   */
  def userHomeTimeline(userID: Long, count: Int): ResponseList[Status] = {
    twitter.getUserTimeline(userID, new Paging(1, count))
  }

  /**
   * つぶやきます
   */
  def postTweet(s: String) = {
    twitter.updateStatus(s)
  }

  /**
   * まだ返事をしていないリプライをcount件取得<br>
   */
  def mentions(count: Int): ResponseList[Status] = {
    twitter.getMentionsTimeline(new Paging(1, count, updator.lastReplyID))
  }

  /**
   * 返事をします<br>
   * inReplyToStatusIdに返事先のツイートIDを指定します<br>
   * isDuplicateをtrueにするとランダムな数字を付与します
   */
  def postReply(reply: String, inReplyToStatusId: Long = -1L, isDuplicate: Boolean = false) = {
    val postReply = if (isDuplicate) reply + " " + random.nextInt(100) else reply
    val statusUpdate = new StatusUpdate(postReply)
    if(inReplyToStatusId > 0){
      statusUpdate.setInReplyToStatusId(inReplyToStatusId)
    }
    twitter.updateStatus(statusUpdate)
    updator.updateLastReplyID(inReplyToStatusId)
  }

  /**
   * 返事をしないツイートIDを通知します
   */
  def doNotReplyToThisTweet(statusID: Long) = {
    updator.updateLastReplyID(statusID)
  }

  /**
   * リツイートします
   */
  def retweetStatus(status: Status) = {
    twitter.retweetStatus(status.getId)
  }

}

object TwitterAPI {
  val tweetLengthMax = 140
}