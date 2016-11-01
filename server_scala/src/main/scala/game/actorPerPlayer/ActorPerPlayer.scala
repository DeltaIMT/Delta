package game.actorPerPlayer

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage.Command
import game.GameEvent.{Angle, Player, PlayerData, PlayerMessage, PlayersUpdate, Tick, Vector}
import game.GameEvent.PlayerData.newOne
import play.api.libs.json.Json
import game.Formatters._

/**
  * Created by vannasay on 27/10/16.
  */
class ActorPerPlayer(id: String, playerActorRef: ActorRef) extends Actor{
  val rand = scala.util.Random
  var player = Player(newOne(id, rand), playerActorRef)
  var message = ""

  override def receive: Receive = {
    case Command(id, command) => {
      player = player.setCommand(command)
    }
    case Tick() => {
      player = Player(physic(player.data), player.actor)
      updateMessage
      notifyPlayer
    };
  }

  def physic(data: PlayerData): PlayerData = {
    if(data.lastCommand != null) {
      val jsonObject = Json.parse(data.lastCommand)
      val mouse_x = (jsonObject \ "mouse" \ "x").as[Double]
      val mouse_y = (jsonObject \ "mouse" \ "y").as[Double]
      val direction2go = Vector(mouse_x, mouse_y) - data.p.head
      var newAngle  = 0.0

      val vectorAngle = Vector(Math.cos(data.angle), Math.sin(data.angle))
      val MeanVector =( direction2go.unit*0.2 + vectorAngle*0.8).unit

      newAngle = Angle.arctan(MeanVector.x,MeanVector.y)

      val newSpeed = Vector.fromAngle(data.angle)*5

      var remove = 1
      if (data.p.size < data.l)
        remove = 0
      val newPositions =data.p.head +newSpeed  ::data.p.take(data.p.size-remove)

      return PlayerData(data.id,newPositions, data.v,newAngle,data.l, data.r, data.color, data.lastCommand  )
    }
    else
      return data
  }

  def updateMessage() : Unit = {
    message = playerToJson(player.data)
  }

  def notifyPlayer(): Unit = {
    player.actor ! PlayersUpdate("["+message+"]")
  }

  def playerToJson(data: PlayerData): String = {
    val message = PlayerMessage(data.id, data.p.head.x ,data.p.head.y , data.r,data.l, data.color)
    val jsonMessage = Json.toJson(message)
    Json.stringify(jsonMessage)
  }
}
