package game.spatialHost

import akka.actor.{Actor, ActorRef}
import core.CoreMessage._
import game.GameEvent.{AddPlayer, AddPlayerData, Angle, Player, PlayerData, PlayerMessage, PlayersUpdate, Tick, Vector}
import play.api.libs.json.Json
import game.Formatters._

case class OtherSpatial(other: ActorRef, where: String)

class SpatialHost(val position: Vector, val dimension: Vector, val factor: Double) extends Actor {
  var players = collection.mutable.LinkedHashMap.empty[String, Player]
  var halfPlayerData = collection.mutable.LinkedHashMap.empty[String, PlayerData]
  var halfPlayerActor = collection.mutable.LinkedHashMap.empty[String, ActorRef]
  val rand = scala.util.Random
  var other: ActorRef = null
  var where = ""
  var provider: ActorRef = null

  override def receive: Receive = {
    case AddClient(id: String, client: ActorRef) => {
      if (halfPlayerData.keySet.exists(_ == id) && halfPlayerData(id) != null)
        players += (id -> Player(halfPlayerData(id), client))
      else
        halfPlayerActor += id -> client
    }
    case AddPlayerData(pd: PlayerData) => {
      if (halfPlayerActor.keySet.exists(_ == pd.id) &&  halfPlayerActor(pd.id) != null)
        players += (pd.id -> Player(pd, halfPlayerActor(pd.id)))
      else
        halfPlayerData += pd.id -> pd
    }

    case DeleteClient(id: String) => players -= id
    case Command(id, command) => {
      if(players.keySet.exists(_ == id) && players(id)!= null)
      players(id) = players(id).setCommand(command)
    }
    case Tick() => {
      physics
      notifyPlayers
      changeIsNeeded
    }

    case SetProvider(actor: ActorRef) => provider = actor
    case OtherSpatial(other: ActorRef, where: String) => {
      this.other = other
      this.where = where
    }
  }

  def isInside(p: Player): Boolean = {
    val player_pos = p.data.p.head;

    ((position.x < player_pos.x)
      && (position.y < player_pos.y)
      && (player_pos.x <= position.x + dimension.x)
      && (player_pos.y <= position.y + dimension.y)
      )
  }


  def players_out(): collection.mutable.LinkedHashMap[String, Player] = {
    val players_to_update = collection.mutable.LinkedHashMap.empty[String, Player]
    players.foreach(paire => {
      if (!(isInside(paire._2))) {
        players_to_update.+=(paire)
      }
    })
    players_to_update
  }


  def changeIsNeeded(): Unit = {
    val playerOut = players_out
    playerOut.foreach(paire => {
      changeArea(paire._2)
    })

  }


  def changeArea(p: Player): Unit = {

    val player_pos = p.data.p.head;
    if (player_pos.x > position.x + dimension.x && where == "E") {
      provider ! ChangeActor(p.data.id, other)
      other ! AddPlayerData(p.data)
      this.players -= p.data.id
    }
    else if (player_pos.x < position.x && where == "W") {
      provider ! ChangeActor(p.data.id, other)
      other ! AddPlayerData(p.data)
      this.players -= p.data.id
    }
    else
      players(p.data.id) = p.bloc
    //other ! AddClient(p.data.id, p.actor)
    //this.players.remove(p.data.id)
  }


  def physics(): Unit = {
    players.foreach { case (s: String, p: Player) => players(s) = Player(physic(p.data), p.actor) }
    //players.foreach { case (s: String, p: Player) => println(p.data.p.toString())}
    val playersValues = players.values

    playersValues.foreach(x => {
      val c = collision(x, playersValues.filter(y => x != y))
      if (c != "0") {

        players(x.data.id) = players(x.data.id).newPosAng(Vector(rand.nextInt(800), rand.nextInt(800)), rand.nextDouble())

      }
    })
  }

  def physic(playerData: PlayerData): PlayerData = {
    if (playerData.lastCommand != null) {
      val jsonObject = Json.parse(playerData.lastCommand)
      val mouse_x = (jsonObject \ "mouse" \ "x").as[Double]
      val mouse_y = (jsonObject \ "mouse" \ "y").as[Double]
      val direction2go = Vector(mouse_x, mouse_y) - playerData.p.head
      var newAngle = 0.0
      val vectorAngle = Vector(Math.cos(playerData.angle), Math.sin(playerData.angle))
      val MeanVector = (direction2go.unit * 0.2 + vectorAngle * 0.8).unit
      newAngle = Angle.arctan(MeanVector.x, MeanVector.y)
      val newSpeed = Vector.fromAngle(playerData.angle) * 5
      var remove = 1
      if (playerData.p.size < playerData.l)
        remove = 0
      val newPositions = playerData.p.head + newSpeed * factor :: playerData.p.take(playerData.p.size - remove)

      return PlayerData(playerData.id, newPositions, playerData.v, newAngle, playerData.l, playerData.r, playerData.color, playerData.lastCommand)
    }
    else
      return playerData
  }

  def notifyPlayers(): Unit = {
    //   println("Telling " + players.size + " players the updates")
    var s = ""
    val list = players.values.map(_.data)
    if (list.size == 1) {
      s = "[" + playerToJson(list.head) + "]"
    }
    else if (list.size > 1) {
      s += "["
      var listString = list.map(playerToJson(_))
      s += listString.head
      listString = listString.drop(1)
      for (elem <- listString) {
        s += "," + elem
      }
      s += "]"
    }
    players.values.foreach(_.actor ! PlayersUpdate(s))
  }

  //method which gives a bool in order to know if the player p is hurting one of the other players (players)
  def collision(p: Player, players: Iterable[Player]): String = {
    var collision = "0"
    val initPosition = p.data.p.head
    // for each players, we first see if p is close enough to collide with.
    players.foreach(player_i => {
      val position = player_i.data.p.head
      val length = player_i.data.l * player_i.data.v
      if ((initPosition - position).length < length) {
        var minimumDistance = length
        var closestPoint = position

        // in order to see that, we first look after the player's element which is the closest to p, and save that point in pt1
        player_i.data.p.foreach(eachPoint => {
          if ((initPosition - eachPoint).length < minimumDistance) {
            closestPoint = eachPoint
            minimumDistance = (initPosition - eachPoint).length
          }
        })
        //finally, we conclude on the collision : p is too close to pt1
        if ((initPosition - closestPoint).length < p.data.r + player_i.data.r) {
          collision = player_i.data.id;
        }
      }
    }
    )
    // and we don't forget to return collision, which is the result ;)
    collision
  }

  def playerToJson(player: PlayerData): String = {
    val message = PlayerMessage(player.id, player.p.head.x, player.p.head.y, player.r, player.l, player.color)
    val jsonMessage = Json.toJson(message)
    Json.stringify(jsonMessage)
    //    val messages =player.p.map( x =>  Json.stringify(Json.toJson( PlayerMessage(player.id, x, player.r, player.color) )  ))
    //    var jsonMessage = messages.head
    //    messages.tail.foreach(  x => jsonMessage =x ++ ","++ jsonMessage  )
    //  jsonMessage

  }

}


