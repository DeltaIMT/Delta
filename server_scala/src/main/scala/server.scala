import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.server.Directives
import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Props
import scala.concurrent.duration._
import play.api.libs.json._

trait GameEvent
case class PlayerData(id : String, p:  Iterable[Vector], v:  Double, l : Double, r : Double, color : Array[Int], lastCommand : String )extends GameEvent
case class Player(data : PlayerData, actor : ActorRef)extends GameEvent
{
  def setCommand(command : String) = Player( PlayerData(this.data.id,data.p,data.v,data.l,data.r,data.color,command ), actor )
}
case class AddPlayer(playerData : PlayerData, actorRef : ActorRef)extends GameEvent
case class DelPlayer(id : String)extends GameEvent
case class Command(id : String,command : String )extends GameEvent
case class PlayersUpdate(json : String)extends GameEvent
case class Vector(x: Double, y : Double)
{
  def * (scale : Double) = Vector ( x *scale ,y *scale)
  def * (v : Vector) = Vector ( x *v.x ,y *v.y)
  def + (offset : Vector) = Vector ( x + offset.x ,y + offset.y)
  def - (offset : Vector) = Vector ( x - offset.x ,y - offset.y)
  def clamp(min : Double, max : Double) = Vector (  Math.min(Math.max(x ,0),500) ,Math.min(Math.max(y ,0),500))
  def unit =
  {
    var Result = Vector(1,0)
    var length = this.length
    if (length != 0)
    {
      val lengthInv = 1/ length
      Result = Vector( x* lengthInv, y * lengthInv)
    }
    Result
  }
  def length = Math.sqrt(x*x+y*y)
}
case class Tick()

class Region extends Actor {
  var players = collection.mutable.LinkedHashMap.empty[String, Player]
  var time= 0

  override def receive: Receive = {
    case AddPlayer(player, actor) => players += (player.id -> Player(player, actor))
    case DelPlayer(id) => println("Deleting " + id); players.remove(id)
    case Command(id, command) => {
      players(id) = players(id).setCommand(command)
    }
    case Tick() => physics();physics();notifyPlayers()
  }

  def physics() : Unit   =
  {
    time+=1
    var playersList = players.values



    while (playersList.size >1)
    {
      val a = playersList.head
        playersList.tail.foreach( b =>  {

        var vector = a.data.p - b.data.p
        var length = vector.length
        var intersect = (a.data.r + b.data.r) - length
        val vectorUnit = vector.unit


        if (intersect > 0) {
          if(intersect > 5) intersect =  5
          val oldPlayerA = a.data
          players(oldPlayerA.id) = Player(PlayerData(oldPlayerA.id, oldPlayerA.p + (vectorUnit * intersect * 0.5), oldPlayerA.v + (vectorUnit * intersect * 1), oldPlayerA.r, oldPlayerA.color, oldPlayerA.lastCommand), a.actor)

          val oldPlayerB = b.data
          players(oldPlayerB.id) = Player(PlayerData(oldPlayerB.id, oldPlayerB.p - (vectorUnit * intersect * 0.5), oldPlayerB.v - (vectorUnit * intersect * 1), oldPlayerB.r, oldPlayerB.color, oldPlayerB.lastCommand), b.actor)
        }

      })

      playersList = playersList.tail
    }


    players.foreach{ case (s: String,p : Player)   => physic(s,p) }
  }

  def physic(s : String, p : Player): Unit =
  {
    if(p.player.lastCommand != null) {
      val objcom = Json.parse(p.player.lastCommand)
      val mouse_x = (objcom \ "mouse" \ "x").as[Double]
      val mouse_y = (objcom \ "mouse" \ "y").as[Double]
      val oldPlayerActor = p
      val oldPlayer = oldPlayerActor.player
      val actor = oldPlayerActor.actor
      val direction2go = Vector(mouse_x, mouse_y) - oldPlayer.p
      val newSpeed = (oldPlayer.v + (direction2go * 0.1)) * 0.8
      players(s) = Player(PlayerData(s, oldPlayer.p + newSpeed, newSpeed, 40*(1+0.2*Math.  sin(time/10.0)), oldPlayer.color, oldPlayer.lastCommand), actor)
    }
  }

  def notifyPlayers(): Unit = {
    println("Telling " + players.size + " players the updates")
    var s = ""
    val list = players.values.map(_.player)
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

  def playerToJson(player: PlayerData): String = "{\"id\":\""+player.id+"\",\"pos\":["+player.p.x+","+player.p.y+"],\"r\":"+player.r+",\"color\":["+player.color(0) +","+player.color(1) +","+player.color(2) +"]" + "}"
}

object server extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val region  = actorSystem.actorOf(Props[Region], "region")
  val region1 = actorSystem.actorOf(Props[Region], "region1")
  val cancellable  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, region, Tick())
  val cancellable1  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, region1, Tick())
  val regionMap = Map[String, ActorRef](("region", region), ("region1", region1))


  import Directives._
  val ws = new WebSocket(regionMap)
  val route = (get & parameter("id") ){id =>  handleWebSocketMessages(ws.flow(id, "region"))}
  val interface ="localhost"
  val port = 8080
  val binding = Http().bindAndHandle(route, "0.0.0.0", port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()
  cancellable.cancel()
  cancellable1.cancel()
  actorSystem.terminate()
  println("Server is down...")
}


