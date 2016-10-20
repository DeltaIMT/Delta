import akka.actor.Actor
import play.api.libs.json.Json

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
    players.foreach{ case (s: String,p : Player)   => physic(s,p) }
  }

  def physic(s : String, p : Player): Unit =
  {
    if(p.data.lastCommand != null) {
      val objcom = Json.parse(p.data.lastCommand)
      val mouse_x = ((objcom \ "mouse" \ "x").as[Double])
      val mouse_y = ((objcom \ "mouse" \ "y").as[Double])
      val oldPlayer = p.data
      val direction2go = Vector(mouse_x, mouse_y) - oldPlayer.p.head
      val newSpeed = Vector.fromAngle(oldPlayer.angle)*1
      players(s) = players(s).newPos(oldPlayer.p.head + newSpeed)
    }
  }

  def notifyPlayers(): Unit = {
    println("Telling " + players.size + " players the updates")
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

  def playerToJson(player: PlayerData): String = "{\"id\":\""+player.id+
    "\",\"pos\":["+player.p.head.x+
    ","+player.p.head.y+"],\"r\":"+player.r+
    ",\"color\":["+player.color(0) +","+
    player.color(1) +","+player.color(2) +
    "]" + "}"
}
