import akka.actor.ActorRef


trait GameEvent
case class PlayerData(id : String, p:  List[Vector], v:  Double,angle: Double, l : Double, r : Double, color : Array[Int], lastCommand : String )extends GameEvent
case class Player(data : PlayerData, actor : ActorRef)extends GameEvent
{
  def setCommand(command : String) = Player( PlayerData(this.data.id,data.p,data.v,data.angle,data.l,data.r,data.color,command ), actor )
  def newPos(pos : Vector) =
    {
      var newPositions =pos ::data.p.take(data.p.size)
      Player( PlayerData(this.data.id,newPositions,data.v,data.angle,data.l,data.r,data.color,data.lastCommand ), actor )
    }


}
case class AddPlayer(playerData : PlayerData, actorRef : ActorRef)extends GameEvent
case class DelPlayer(id : String)extends GameEvent
case class Command(id : String,command : String )extends GameEvent
case class PlayersUpdate(json : String)extends GameEvent

object Vector
{
  def fromAngle(angle : Double) = Vector(Math.cos(angle), Math.sin(angle))
}

case class Vector(x: Double, y : Double)
{
  def * (arg : Any) : Vector =
    {
      case scale : Double => Vector( x *scale ,y *scale)
      case v : Vector => Vector ( x *v.x ,y *v.y)
    }
  def + (offset : Vector)  : Vector = Vector ( x + offset.x ,y + offset.y)
  def - (offset : Vector)  : Vector = Vector ( x - offset.x ,y - offset.y)
  def clamp(min : Double, max : Double)  : Vector = Vector (  Math.min(Math.max(x ,0),500) ,Math.min(Math.max(y ,0),500))
  def unit =
  {
    var Result = Vector(1,0)
    var length = this.length;
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