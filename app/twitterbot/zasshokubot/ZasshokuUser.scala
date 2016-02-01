package twitterbot.zasshokubot

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ZasshokuUser(_userID: Long, _screenName: String, _isProtected: Boolean, _totalExp: Int = 0) {
  val userID = _userID
  var screenName = _screenName
  var isProtected = _isProtected
  var level = 1
  var totalExp = 0
  private var nextLevelUpTotalExp = ZasshokuUser.calcNextLevelUpTotalExp(level)

  def gainExp(gainedExp: Int) = {
    totalExp += gainedExp
    val preLevel = 0
    while (totalExp >= nextLevelUpTotalExp) {
      level += 1
      nextLevelUpTotalExp = ZasshokuUser.calcNextLevelUpTotalExp(level)
    }
    (preLevel, level)
  }

  def nextLevelUpExp() = {
    nextLevelUpTotalExp - totalExp
  }
}

object ZasshokuUser {
  private def calcNextLevelUpTotalExp(level: Int): Int = {
    var returnExp = 0
    for (n: Int <- Range(1, level + 1)) {
      returnExp += Math.ceil(1.1 * n).toInt
    }
    return returnExp
  }
  
  implicit val jsonWrite = Json.writes[ZasshokuUser]
  implicit val jsonReads = Json.reads[ZasshokuUser]
}