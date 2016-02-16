package twitterbot.zasshokubot

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ZasshokuUser(_userID: Long, _screenName: String, _isProtected: Boolean, _totalExp: Int = 0) {
  val userID = _userID
  var screenName = _screenName
  var isProtected = _isProtected
  var level = 1
  var totalExp = _totalExp
  private var nextLevelUpTotalExp = ZasshokuUser.calcNextLevelUpTotalExp(level)

  def gainExp(gainedExp: Int) = {
    totalExp += gainedExp
    val preLevel = level
    while (totalExp >= nextLevelUpTotalExp) {
      level += 1
      nextLevelUpTotalExp = ZasshokuUser.calcNextLevelUpTotalExp(level)
    }
    (preLevel, level)
  }

  def nextLevelUpExp() = {
    nextLevelUpTotalExp - totalExp
  }
  
  def toCSV(): String = {
    userID.toString + "," + 
    screenName.toString + "," + 
    isProtected.toString + "," + 
    totalExp.toString
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
  
  def parseCSV(csv: String): ZasshokuUser = {
    val array = csv.split(",")
    val userID = java.lang.Long.parseLong(array(0))
    val screenName = array(1)
    val isProtected = java.lang.Boolean.parseBoolean(array(2))
    val totalExp = java.lang.Integer.parseInt(array(3))
    new ZasshokuUser(userID, screenName, isProtected, totalExp)
  }
}