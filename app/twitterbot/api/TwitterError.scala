package twitterbot.api

/**
 * Twitterのエラーコード
 */
object TwitterError {
  val couldNotFollowByAlreadyRequested = 160
  val statusOver140Characters = 186
  val duplicateStatus = 187
}