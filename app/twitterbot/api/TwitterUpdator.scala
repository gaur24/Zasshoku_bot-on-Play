package twitterbot.api

import java.nio.file.Path
import java.nio.file.Files
import scala.collection.JavaConversions._
import java.nio.file.StandardOpenOption
import java.util.List
import java.util.ArrayList
import java.util.Arrays

class TwitterUpdator(_pNotFollow: Path, _pNotUnfollow: Path, _pLastReply: Path, _pLastTimeline: Path) {
  private val pNotFollowUserIDs = _pNotFollow
  private val pNotUnfollowUserIDs = _pNotUnfollow
  private val pLastReplyID = _pLastReply
  private val pLastHomeTimelineID = _pLastTimeline

  var notUnfollowUserIDs = TwitterUpdator.readLongs(pNotUnfollowUserIDs)
  var notFollowUserIDs = TwitterUpdator.readLongs(pNotFollowUserIDs)
  var lastReplyID = TwitterUpdator.readLongs(pLastReplyID).get(0)
  var lastHomeTimelineID = TwitterUpdator.readLongs(pLastHomeTimelineID).get(0)

  def addNotFollowUser(userID: Long) = {
    notFollowUserIDs.add(userID)
    val list = Arrays.asList(userID.toString())
    Files.write(pNotFollowUserIDs, list, StandardOpenOption.APPEND)
  }

  def removeNotFollowUser(userID: Long) = {
    notFollowUserIDs.remove(userID)
    val list = notFollowUserIDs.seq.map { x => x.toString() }
    Files.write(pNotFollowUserIDs, list)
  }

  def addNotUnfollowUser(userID: Long) = {
    notUnfollowUserIDs.add(userID)
    val list = Arrays.asList(userID.toString())
    Files.write(pNotUnfollowUserIDs, list, StandardOpenOption.APPEND)
  }

  def removeNotUnfollowUser(userID: Long) = {
    notUnfollowUserIDs.remove(userID)
    val list = notUnfollowUserIDs.seq.map { x => x.toString() }
    Files.write(pNotUnfollowUserIDs, list)
  }

  def updateLastReplyID(statusID: Long) = {
    if (lastReplyID < statusID) {
      lastReplyID = statusID
      val list = Arrays.asList(statusID.toString())
      Files.write(pLastReplyID, list)
    }
  }

  def updateLastHomeTimelineID(statusID: Long) = {
    if (lastHomeTimelineID < statusID) {
      lastHomeTimelineID = statusID
      val list = Arrays.asList(statusID.toString())
      Files.write(pLastHomeTimelineID, list)
    }
  }
}

object TwitterUpdator {
  private def readLongs(path: Path): List[Long] = {
    Files.readAllLines(path).seq.map { x => x.toLong }
  }

}