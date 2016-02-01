package twitterbot.api

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.Random
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.User
import scala.collection.mutable.WrappedArray

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

  def showUser(userID: Long): User = {
    twitter.showUser(userID)
  }

  def followersIDs(): Seq[Long] = {
    var cursor = -1L
    wrapLongArray(twitter.getFollowersIDs(cursor).getIDs).toList
  }

  def friendsIDs(): Seq[Long] = {
    var cursor = -1L
    wrapLongArray(twitter.getFriendsIDs(cursor).getIDs).toList
  }

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

  def unfollowNotFollowMe() = {
    val followersIDs = this.followersIDs()
    val notFollowMeUsers = this.friendsIDs()
      .filterNot { x => followersIDs.contains(x) }
      .filterNot { x => updator.notUnfollowUserIDs.contains(x) }
      .toList

    notFollowMeUsers.foreach { id => twitter.destroyFriendship(id) }
  }

  def homeTimeline(count: Int): ResponseList[Status] = {
    twitter.getHomeTimeline(new Paging(1, count))
  }

  def homeTimelineDifference(count: Int): ResponseList[Status] = {
    val homeTimeline = twitter.getHomeTimeline(new Paging(1, count, updator.lastHomeTimelineID))
    // TODO どっちか
    //    updator.updateLastHomeTimelineID(homeTimeline.head.getId)
    val id = if(homeTimeline.isEmpty()) 0L else homeTimeline.last.getId
    updator.updateLastHomeTimelineID(id)
    return homeTimeline
  }

  def userHomeTimeline(userID: Long, count: Int): ResponseList[Status] = {
    twitter.getUserTimeline(userID, new Paging(1, count))
  }

  def postTweet(s: String) = {
    twitter.updateStatus(s)
  }

  def mentions(count: Int): ResponseList[Status] = {
    twitter.getMentionsTimeline(new Paging(1, count, updator.lastReplyID))
  }

  def postReply(reply: String, inReplyToStatusId: Long, isDuplicate: Boolean) = {
    val postReply = if (isDuplicate) reply + " " + random.nextInt(100) else reply
    val statusUpdate = new StatusUpdate(postReply)
    statusUpdate.setInReplyToStatusId(inReplyToStatusId)
    twitter.updateStatus(statusUpdate)
    updator.updateLastReplyID(inReplyToStatusId)
  }

  def doNotReplyToThisTweet(statusID: Long) = {
    updator.updateLastReplyID(statusID)
  }

  def retweetStatus(status: Status) = {
    twitter.retweetStatus(status.getId)
  }

}

object TwitterAPI {
  val tweetLengthMax = 140
}

object TwitterError {
  val couldNotFollowByAlreadyRequested = 160
  val statusOver140Characters = 186
  val duplicateStatus = 187
}