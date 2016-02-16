package twitterbot.zasshokubot

import java.nio.file.Path
import java.nio.file.Files
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import play.api.libs.json.Json
import scala.collection.mutable.ListBuffer

class ZasshokuUserManager(_usersPath: Path) {
  private val usersPath = _usersPath
  private var users = ListBuffer() ++= this.readZasshokuUsers

  def find(id: Long): Option[ZasshokuUser] = {
    users.filter(_.userID == id).headOption
  }
  
  def update(user: ZasshokuUser) = {
    users = users.filterNot(_.userID == user.userID) += (user)
    this.writeZasshokuUsers
  }
  
  def getUsers(): Seq[ZasshokuUser] = {
    users.toSeq
  }

  /**
   * ユーザー情報をJSON形式で書き出します
   */
  def writeZasshokuUsers() = {
    val jsons = users.map(Json.toJson(_).toString).toList
    Files.write(usersPath, jsons.asJava)
  }

  /**
   * ユーザー情報をファイルから読み込みます
   */
  def readZasshokuUsers(): Seq[ZasshokuUser] = {
    Files.readAllLines(usersPath).asScala
      .map { x => Json.parse(x).validate[ZasshokuUser].get }.toSeq
  }
}