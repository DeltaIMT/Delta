package game.stgy

import core.user_import.{Element, Observable}

import scala.util.Random


abstract class Unity extends Element(0, 0) with Observable {
  var id: String
  var clientId: String
  var color: Array[Int]
}

abstract class Movable extends Unity {
  var move = false
  var target = Vec()
  var speed = 5

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

trait Damagable {
  var health = 1.0

  def damage(amount : Double) = {
    health = math.max(0,health- amount)
  }

  def isDead :Boolean = health ==0
}

class Bowman(x_ : Double, y_ : Double, id_ : String, clientId_ : String, color_ : Array[Int]) extends Movable with Damagable {

  x = x_
  y = y_
  override var id: String = id_
  override var clientId: String = clientId_
  override var color: Array[Int] = color_

  var canShootIn = 0

  def canShoot = {
    canShootIn == 0
  }

  def shoot(target: Vec): Arrow = {
    canShootIn = 60
    val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
    arrow.move = true
    arrow.target = target
    arrow
  }

  def step() = {
    doMove
    if (canShootIn > 0)
      canShootIn -= 1
  }

}

class Arrow(x_ : Double, y_ : Double, id_ : String, clientId_ : String, color_ : Array[Int]) extends Movable {
  x = x_
  y = y_
  override var id: String = id_
  override var clientId: String = clientId_
  override var color: Array[Int] = color_
  speed = 10

  def shouldDie: Boolean = {
    var toTarget = target - Vec(x, y)
    var length = toTarget.length()
    length < 3
  }

}
