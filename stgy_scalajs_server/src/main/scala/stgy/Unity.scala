package stgy

import akka.actor.FSM.->
import core.observerPattern.{Observable, Observer}
import stgy.StgyTypes.UnitType.UnitType
import stgy.StgyTypes._

import scala.util.Random

case class StgyFrame(units: List[UnityFront],arrows: List[ArrowFront], camX: Float, camY: Float, xp: Float, numberOfUnit: Int)
case class UnityFront(id : String, color : Color, x: Int, y : Int, health: Float)
case class ArrowFront(id : String, x: Int, y : Int)

trait MetaUnit {
  val metaType: UnitType
}

trait Unity extends Element {
  var id: UnitId
  var clientId: ClientId
  var color: Color
}

trait Movable extends Unity {
  var move = false
  var target = Vec()
  var speed: Double

  def getSpeed: Vec = {
    val pos = Vec(x, y)
    val dir = (target - pos).normalize()
    dir * speed * (if (move) 1.0 else 0.0)
  }

  def doMove = {
    if ((Vec(x, y) - target).length() < 1)
      move = false
    if (move) {
      var toTarget = target - Vec(x, y)
      var length = toTarget.length()
      toTarget /= length
      x += toTarget.x * speed * math.min(length / speed, 1)
      y += toTarget.y * speed * math.min(length / speed, 1)
    }
  }
}

trait Damagable extends Unity {
  var health = 1.0
  var maxHealth = 1.0
  var radius: Int
  var xpCost: Double

  def damage(amount: Double) = {
    health = math.max(0, health - amount)
  }

  def damagableStep = {
    health = math.min(maxHealth, health + 0.002 * maxHealth)
  }

  def isDead: Boolean = health <= 0
}

trait Shooter {
  var canShootIn = 0

  def canShoot = {
    canShootIn == 0
  }

  def shooterStep = {
    if (canShootIn > 0)
      canShootIn -= 1
  }
}


class Commander(var x: Double, var y: Double, var id: UnitId, var clientId: ClientId, var color: Color) extends Movable with Damagable with Shooter with MetaUnit {
  val metaType = UnitType.Com
  health = 2
  maxHealth = 2
  override var radius: Int = 25
  var speed = 1.0
  var xpCost = 10.0

  def shoot(target: Unity): List[Arrow] = {
    target match {
      case e: Movable => {
        shoot(Vec(e.x, e.y) + e.getSpeed * 30)
      }
      case e: Damagable => {
        shoot(Vec(e.x, e.y))
      }
    }
  }

  def shoot(target: Vec): List[Arrow] = {
    canShootIn = 60
    val targets = 1 to 10 map { i => target + Vec(Random.nextInt(50) - 25, Random.nextInt(50) - 25) }
    val arrows = targets.map(t => {
      val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color, id)
      arrow.move = true
      val direction = (t - Vec(x, y)).normalize()
      arrow.target = Vec(x, y) + (direction * 1000.0)
      arrow.speed = arrow.speed - 5 + Random.nextInt(10)
      arrow
    })
    arrows.toList
  }

  def step() = {
    doMove
    shooterStep
    damagableStep
  }


}


class Swordman(var x: Double, var y: Double, var id: UnitId, var clientId: ClientId, var color: Color) extends Movable with Damagable with Shooter with MetaUnit {
  val metaType = UnitType.Sword
  override var radius: Int = 20
  maxHealth = 3
  health = 3
  var speed = 2.0
  var xpCost = 3.0
  var damage = 5 * 0.201 / 6.0

  def step() = {
    doMove
    shooterStep
    damagableStep
  }

  def canAttack(e: Damagable): Boolean = {
    val vector = Vec(x, y) - Vec(e.x, e.y)
    vector.length() < 50
  }

  def attack(e: Damagable): Double = {
    canShootIn = 10
    damage
  }

}

class Bowman(var x: Double, var y: Double, var id: UnitId, var clientId: ClientId, var color: Color) extends Movable with Damagable with Shooter with MetaUnit {
  val metaType = UnitType.Bow
  override var radius: Int = 20
  var speed = 1.0
  var xpCost = 1.0

  def shoot(target: Unity): Arrow = {
    target match {
      case e: Movable => {
        shoot(Vec(e.x, e.y) + e.getSpeed * 30)
      }
      case e: Damagable => {
        shoot(Vec(e.x, e.y))
      }
    }
  }

  def shoot(target: Vec): Arrow = {
    canShootIn = 60
    val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color, id)
    arrow.move = true
    val direction = (target - Vec(x, y)).normalize()
    arrow.target = Vec(x, y) + (direction * 1000.0)
    arrow
  }

  def step() = {
    doMove
    shooterStep
    damagableStep
  }
}

class Arrow(var x: Double, var y: Double, var id: UnitId, var clientId: ClientId, var color: Color, var shooterId: UnitId) extends Movable {
  val id2: ClientId = id
  var speed = 10.0
  var frame = 0
  var shotFrom = Vec(x, y)

  def shouldDie: Boolean = {
    frame += 1
    frame == 60
  }

}
