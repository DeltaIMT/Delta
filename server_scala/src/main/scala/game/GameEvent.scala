package game

import akka.actor.ActorRef
import game.GameEvent.{Player, PlayerData, Vector}
import scala.util.Random

object GameEvent {

  case class PlayerData(id: String, p: List[Vector], v: Double, angle: Double, l: Double, r: Double, color: Array[Int], lastCommand: String)

  object PlayerData {
    def newOne(id: String, rand: Random): PlayerData = PlayerData(id, Vector(0, 0) :: List.empty[Vector], 50, 0, 1000, 10, Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), null)
  }

  case class Player(data: PlayerData, actor: ActorRef)  {
    def setLength(l: Int) = Player(PlayerData(this.data.id, data.p, data.v, data.angle, l, data.r, data.color, data.lastCommand), actor)

    def addLength() = Player(PlayerData(this.data.id, data.p, data.v, data.angle, data.l + 10, data.r, data.color, data.lastCommand), actor)

    def setCommand(command: String) = Player(PlayerData(this.data.id, data.p, data.v, data.angle, data.l, data.r, data.color, command), actor)

    def setColor(color: Array[Int]) = Player(PlayerData(this.data.id, data.p, data.v, data.angle, data.l, data.r, color, data.lastCommand), actor)

    def newPosAng(pos: Vector, angle: Double) = {
      var remove = 1
      if (data.p.size < data.l)
        remove = 0
      val newPositions = pos :: data.p.take(data.p.size - remove)
      Player(PlayerData(this.data.id, newPositions, data.v, angle, data.l, data.r, data.color, data.lastCommand), actor)
    }


  }
  case class AddPlayer(playerData: PlayerData, actorRef: ActorRef)
  case class DelPlayer(id: String)
  case class PlayersUpdate(json: String)

  object Vector {
    def fromAngle(angle: Double) = Vector(Math.cos(angle), Math.sin(angle))
  }

  case class Vector(x: Double, y: Double) {
    def *(arg: Any): Vector = arg match {
      case scale: Int => Vector(x * scale, y * scale)
      case scale: Double => Vector(x * scale, y * scale)
      case v: Vector => Vector(x * v.x, y * v.y)
    }

    def +(offset: Vector): Vector = Vector(x + offset.x, y + offset.y)

    def -(offset: Vector): Vector = Vector(x - offset.x, y - offset.y)

    def clamp(min: Double, max: Double): Vector = Vector(Math.min(Math.max(x, 0), 500), Math.min(Math.max(y, 0), 500))

    def unit = {
      var Result = Vector(1, 0)
      val length = this.length;
      if (length != 0) {
        val lengthInv = 1 / length
        Result = Vector(x * lengthInv, y * lengthInv)
      }
      Result
    }

    def length = Math.sqrt(x * x + y * y)

  }

  case class Tick()

  object Angle {
    def modulify(a: Double): Double = {

      var b = a - (a / (2 * Math.PI)).toInt * 2 * Math.PI
      if (Math.abs(b) > Math.PI)
        b = b - Math.signum(b) * 2 * Math.PI
      b
    }

    def arctan(x: Double, y: Double): Double = {

      var a = Math.atan(y / x)

      if (y / x < 0)
        a = a + Math.PI

      if (y < 0)
        a = a + Math.PI

      //    if(x <0)
      //      a+= Math.PI

      modulify(a)
      a
    }

  }

  case class PlayerMessage(id: String, x: Double,y:Double, r: Double, l :Double, rgb: Array[Int])

}
