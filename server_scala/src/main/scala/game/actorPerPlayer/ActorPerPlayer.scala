package game.actorPerPlayer

import akka.actor.{Actor, ActorRef}
import akka.pattern._
import core.CoreMessage.{Command, DeleteClient}
import game.Formatters._
import game.GameEvent._
import play.api.libs.json.Json

/**
  * Created by vannasay on 27/10/16.
  */
class ActorPerPlayer(id: String, playerActorRef: ActorRef) extends Actor{
  val rand = scala.util.Random
  var player = Player( PlayerData(id, Vector(0, 0) :: List.empty[Vector], 50, rand.nextDouble(), 10, 10, Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), null), playerActorRef)
  var message = ""
  var players = collection.mutable.LinkedHashMap.empty[String, ActorRef]

  override def receive: Receive = {
    case Command(id, command) => {
      player = player.setCommand(command)
    }
    case Tick() => {
      player = Player(physic(player.data), player.actor)
      updateMessage
      notifyPlayer
    };

    case ListPlayers(list) => {
      players = list
      players.foreach(_._2 ! NewPlayer(id, playerActorRef))
    }

    case NewPlayer(id, playerActorRef) => {
      players += id -> playerActorRef
    }

    case DeleteClient(id) => {
      players.foreach(_._2 ! DeletePlayer(id))
    }

    case DeletePlayer(id) => {
      players -= id
    }

    case AskJson(id) => {
      players(id) ! PlayerJson(playerToJson(player.data))
    }
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
    var listPositions = List[String]()
    val positions = players.map(actor => actor._2 ? AskJson(id))
    positions.foreach(_.foreach(msg => {
      case PlayerJson(json) => listPositions = json :: listPositions
    }))
    var msg = ""
    if (players.size == 1){
      msg = "[" + listPositions.head + "]"
    }
    else {
      msg = "[" + listPositions.head
      listPositions = listPositions.drop(1)
      for (elem <- listPositions) {
        msg += "," + elem
      }
      msg += "]"
    }
    player.actor ! PlayersUpdate(msg)
  }

  def playerToJson(data: PlayerData): String = {
    val message = PlayerMessage(data.id, data.p.head.x ,data.p.head.y , data.r,data.l, data.color)
    val jsonMessage = Json.toJson(message)
    Json.stringify(jsonMessage)
  }
}
