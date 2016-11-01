package game

import game.GameEvent.PlayerMessage
import game.GameEvent._
import play.api.libs.json._

object Formatters {
  implicit val VectorFormat = Json.format[Vector]
  implicit val PlayerMessageFormat = Json.format[PlayerMessage]
}