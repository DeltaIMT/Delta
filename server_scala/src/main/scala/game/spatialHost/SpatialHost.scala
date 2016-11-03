package game.spatialHost

import akka.actor.{Actor, ActorRef}
import akka.pattern._
import akka.util.Timeout
import core.CoreMessage._
import game.Formatters._
import game.GameEvent.{AddPlayerData, Angle, Player, PlayerData, PlayerJson, PlayerMessage, PlayersUpdate, Tick, Vector}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class OtherSpatial(other: ActorRef, where: String)
case class SayPos(where : String)
case class SayPosAll()
case class SetReadList(list : List[ActorRef])
case class Message( json : String )
case class AskMessage()

class SpatialHost(val position: Vector, val dimension: Vector, val factor: Double) extends Actor {



  var players = collection.mutable.LinkedHashMap.empty[String, Player]
  var halfPlayerData = collection.mutable.LinkedHashMap.empty[String, PlayerData]
  var halfPlayerActor = collection.mutable.LinkedHashMap.empty[String, ActorRef]
  val rand = scala.util.Random
  var adjacent = collection.mutable.LinkedHashMap.empty[String, ActorRef]
  var readList : List[ActorRef] = null
  var provider: ActorRef = null
  var message =""


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
      updateMessage
      changeIsNeeded
    }

    case SetProvider(actor: ActorRef) => provider = actor
    case OtherSpatial(other: ActorRef, where: String) => {
      adjacent += where -> other
    }

    case SayPos(where:String) => {
      println(where + " x:" + position.x + " " + "y:" + position.y)
    }
    case SayPosAll() => {
      println("x:"+position.x + " " +"y:"+position.y)
      adjacent.foreach( x => x._2  ! SayPos(x._1))
    }

    case SetReadList(list :List[ActorRef] ) => readList = list

    case AskMessage() => {
      sender ! Message(message)
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

  def transfert(id:String,where : String ): Unit ={
    val p = players(id)
    provider ! ChangeActor(p.data.id, adjacent(where))
    adjacent(where) ! AddPlayerData(p.data)
    this.players -= p.data.id

  }

  def changeArea(p: Player): Unit = {

    val player_pos = p.data.p.head;
    val x = player_pos.x
    val y = player_pos.y
    val minX = position.x
    val maxX = position.x + dimension.x
    val minY = position.y
    val maxY = position.y + dimension.y

    if( x > minX && x < maxX && y > maxY  ){
      if( adjacent.keySet.exists(_=="N") )
      transfert(p.data.id,"N")
      else players(p.data.id) = p.bloc
    }
    else if( x> maxX && y > maxY  ){
      if( adjacent.keySet.exists(_=="NE") )
        transfert(p.data.id,"NE")
      else players(p.data.id) = p.bloc
    }
    else if( x> maxX &&y > minY &&y < maxY  ){
      if( adjacent.keySet.exists(_=="E") )
        transfert(p.data.id,"E")
      else players(p.data.id) = p.bloc
    }
    else if( x> maxX &&y < minY ){
      if( adjacent.keySet.exists(_=="SE") )
        transfert(p.data.id,"SE")
      else players(p.data.id) = p.bloc
    }
    else if(  x > minX && x < maxX  &&y < minY ){
      if( adjacent.keySet.exists(_=="S") )
        transfert(p.data.id,"S")
      else players(p.data.id) = p.bloc
    }
    else if(  x < minX && y < minY ){
      if( adjacent.keySet.exists(_=="SW") )
        transfert(p.data.id,"SW")
      else players(p.data.id) = p.bloc
    }
    else if(  x < minX && y > minY &&  y < maxY   ){
      if( adjacent.keySet.exists(_=="W") )
        transfert(p.data.id,"W")
      else players(p.data.id) = p.bloc
    }
    else if(  x < minX && y > maxY   ){
      if( adjacent.keySet.exists(_=="NW") )
        transfert(p.data.id,"NW")
      else players(p.data.id) = p.bloc
    }


  }


  def physics(): Unit = {
    players.foreach { case (s: String, p: Player) => players(s) = Player(physic(p.data), p.actor) }
    //players.foreach { case (s: String, p: Player) => println(p.data.p.toString())}
    val playersValues = players.values

    playersValues.foreach(x => {
      val c = collision(x, playersValues.filter(y => x != y))
      if (c != "0") {

        players(x.data.id) = players(x.data.id).newPosAng(Vector(50*rand.nextInt(490),50+1*rand.nextInt(490)), rand.nextDouble())

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


  def updateMessage() = {
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
    message = s
  }

  def notifyPlayers(): Unit = {

    var messages = ""

    implicit val timeout = Timeout(1.second)

    var readlistOnlyAdj = List[ActorRef]()
    adjacent.foreach(x => readlistOnlyAdj= x._2 :: readlistOnlyAdj)
    readlistOnlyAdj = self::readlistOnlyAdj

    val listMessage = /*readList*/  readlistOnlyAdj.map(actor => actor ? AskMessage())

    val seq = listMessage.toSeq
    val truc = waitAll(seq)

    truc.foreach(x => {
      x.foreach(trie => trie match {
        case Success(v) => v match {
          case Message(json) =>   {

            if(json !="") {
              if (messages == "") messages = json
              else
                messages = messages.dropRight(1) + "," + json.drop(1)
            }
          }
        }
        case Failure(e) => {}
      })
      players.values.foreach(_.actor ! PlayersUpdate(messages))
    })


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

   def lift[T](futures: Seq[Future[T]]) =
    futures.map(_.map {
      Success(_)
    }.recover { case t => Failure(t) })

  def waitAll[T](futures: Seq[Future[T]]) =
    Future.sequence(lift(futures)) // having neutralized exception completions through the lifting, .sequence can now be used



}


