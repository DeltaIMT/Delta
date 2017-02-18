package splatoon

import scala.util.Random

trait Unity extends Element{
  var id: String
  var clientId: String
  var color: Int
}

trait Movable extends Unity {
  var move = false
  var target = Vec()
  var speed : Double

  def getSpeed : Vec = {
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

class Shooter(var x : Double,var y : Double,var id : String, var clientId : String,var color :Int) extends Movable {
  var speed = 1.0
  var z = false
  var s = false
  var q = false
  var d = false
  var hit = 0
  var space = false

}

class Ball(var x : Double,var y : Double,val vx: Double,val vy : Double,var tx : Double, var ty: Double, val color : Int, val shooterId: String) extends Element{
  var id = Random.alphanumeric.take(10).mkString
  var hasHitPlayer = false

  var lifeTime = 70
  def step = {
     lifeTime -= 1
    x += vx
    y += vy
    if( ((Vec(tx,ty)-Vec(x,y)).length() < 10) || hasHitPlayer )
      lifeTime = 0

  lifeTime <= 0
  }
}