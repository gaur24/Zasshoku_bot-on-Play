package models

case class ZasshokuParameters(
    isEnableTweet: Boolean, delayTweet: Int, periodTweet: Int,
    isEnableReply: Boolean, delayReply: Int, periodReply: Int, replyLimit: Int, replyCount: Int,
    zassyokuRatio: Int, zassyokuID: Long, responseTime: Int)