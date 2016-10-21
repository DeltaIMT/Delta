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

  def physics() : Unit   = {
    time += 1
    players.foreach { case (s: String, p: Player) => physic(s, p) }
    //players.foreach { case (s: String, p: Player) => println(p.data.p.toString())}
  }

  def physic(s : String, p : Player): Unit =
  {
    if(p.data.lastCommand != null) {
      val objcom = Json.parse(p.data.lastCommand)
      val mouse_x = ((objcom \ "mouse" \ "x").as[Double])
      val mouse_y = ((objcom \ "mouse" \ "y").as[Double])
      val oldPlayer = p.data
      val direction2go = Vector(mouse_x, mouse_y) - oldPlayer.p.head
      val dir = (  Vector(mouse_x,mouse_y) - oldPlayer.p.head)
      var newAngle = Angle.modulify(oldPlayer.angle)*0.0 + Angle.arctan(dir.x,dir.y)*1
      val newSpeed = Vector.fromAngle(oldPlayer.angle)*1
      players(s) = players(s).newPosAng(oldPlayer.p.head + newSpeed,newAngle)
    }
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
  def collision(p : Player, players : Iterable[Player] ):Boolean =
  {
    var c = false
    val pos = p.data.p.head
    val l = p.data.l
    // for each players, we first see if p is close enough to collide with.
    players.foreach( pi=>
    {
      val posi = pi.data.p.head
      val li = pi.data.l
      if((pos-posi).length < li){
        var l1 = li
        var pt1 = posi
        // in order to see that, we first look after the player's element which is the closest to p, and save that point in pt1
        pi.data.p.foreach(pt =>
        {
          if ((pos-pt).length < l1){
            pt1 = pt
            l1 = (pos-pt).length
          }
        })
        //finally, we conclude on the collision : p is too close to pt1
        if ((pos-pt1).length < p.data.r + pi.data.r ){
          c = true;
        }
      }
    }
    )
    // and we don't forget to return c, which is the result ;)
    c
  }


  def playerToJson(player: PlayerData): String = "{\"id\":\""+player.id+
    "\",\"pos\":["+player.p.head.x+
    ","+player.p.head.y+"],\"r\":"+player.r+
    ",\"color\":["+player.color(0) +","+
    player.color(1) +","+player.color(2) +
    "]" + "}"
}
