package twitterbot.api

/**
 * Twitterのエラーコード
 */
object TwitterError {
  val couldNotFollowByAlreadyRequested = 160
  val statusOver140Characters = 186
  val duplicateStatus = 187
  val overCapacity = 130  // The Twitter servers are up
  val rateLimitExceeded = 88
  val userNotFound = 50
}