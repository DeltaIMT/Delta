package game.MonoActor

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, Command}
import game.Formatters._
import game.GameEvent._
import play.api.libs.json.Json

class MonoActor extends Actor {
  var players = collection.mutable.LinkedHashMap.empty[String, Player]
  var time= 0
  val rand = scala.util.Random

  override def receive: Receive = {

    case AddClient(id :String , client : ActorRef) => {
      val playerData = PlayerData(id, Vector(0, 0) :: List.empty[Vector], 50, 0, 10, 10, Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)),null)
      players += (id -> Player(playerData, client))
    }

    case Command(id, command) => {
      players(id) = players(id).setCommand(command)
    }
    case Tick() => {
      physics
      notifyPlayers
    };
}

  def physics() : Unit   = {
    time += 1
    players.foreach { case (s: String, p: Player) => players(s) = Player(physic(p.data),p.actor) }
    //players.foreach { case (s: String, p: Player) => println(p.data.p.toString())}
    val playersValues = players.values

    playersValues.foreach(  x => {
      val c = collision(x, playersValues.filter(  y => x!=y ))
      if(c != "0")
        {

          players(x.data.id) = players(x.data.id).newPosAng(Vector(rand.nextInt(500),rand.nextInt(500)),rand.nextDouble())
          players(x.data.id) = players(x.data.id).setLength(10)
          players(c) = players(c).addLength

        }
    })
  }

  def physic(playerData : PlayerData): PlayerData =
  {
    if(playerData.lastCommand != null) {
      val jsonObject = Json.parse(playerData.lastCommand)
      val mouse_x = (jsonObject \ "mouse" \ "x").as[Double]
      val mouse_y = (jsonObject \ "mouse" \ "y").as[Double]
      val direction2go = Vector(mouse_x, mouse_y) - playerData.p.head
      var newAngle  = 0.0

      val vectorAngle = Vector(Math.cos(playerData.angle), Math.sin(playerData.angle))
      val MeanVector =( direction2go.unit*0.2 + vectorAngle*0.8).unit

      newAngle = Angle.arctan(MeanVector.x,MeanVector.y)

      val newSpeed = Vector.fromAngle(playerData.angle)*5

      var remove = 1
      if (playerData.p.size < playerData.l)
        remove = 0
      val newPositions =playerData.p.head +newSpeed  ::playerData.p.take(playerData.p.size-remove)

    return PlayerData(playerData.id,newPositions, playerData.v,newAngle,playerData.l, playerData.r, playerData.color, playerData.lastCommand  )
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
  def collision(p : Player, players : Iterable[Player] ):String =
  {
    var collision = "0"
    val initPosition = p.data.p.head
    // for each players, we first see if p is close enough to collide with.
    players.foreach( player_i=>
    {
      val position = player_i.data.p.head
      val length = player_i.data.l*player_i.data.v
      if((initPosition-position).length < length){
        var minimumDistance = length
        var closestPoint = position

        // in order to see that, we first look after the player's element which is the closest to p, and save that point in pt1
        player_i.data.p.foreach(eachPoint =>
        {
          if ((initPosition-eachPoint).length < minimumDistance){
            closestPoint = eachPoint
            minimumDistance = (initPosition-eachPoint).length
          }
        })
        //finally, we conclude on the collision : p is too close to pt1
        if ((initPosition-closestPoint).length < p.data.r + player_i.data.r ){
          collision = player_i.data.id  ;
        }
      }
    }
    )
    // and we don't forget to return c, which is the result ;)
    collision
  }


  /*def playerToJson(player: PlayerData): String = "{\"id\":\""+player.id+
    "\",\"pos\":["+player.p.head.x+
    ","+player.p.head.y+"],\"r\":"+player.r+
    ",\"color\":["+player.color(0) +","+
    player.color(1) +","+player.color(2) +
    "]" + "}"*/

  def playerToJson(player: PlayerData): String = {
//    val message = PlayerMessage(player.id, player.p.head, player.r, player.color)
//    val jsonMessage = Json.toJson(message)
//    Json.stringify(jsonMessage)
    val messages =player.p.map( x =>  Json.stringify(Json.toJson( PlayerMessage(player.id, x, player.r, player.color) )  ))
    var jsonMessage = messages.head
    messages.tail.foreach(  x => jsonMessage =x ++ ","++ jsonMessage  )
    jsonMessage

  }
}
