package rebound

import core.CoreMessage.CallTrace
import core.user_import.{Element, Observable}
import core.{AbstractMain, Host}

import scala.concurrent.duration._
import scala.swing._
import scala.concurrent.ExecutionContext.Implicits.global

object Rbnd extends App{

  val main = new AbstractMain[RbndHost, RbndProvider]()
  main.numberOfClient = 100
  main.launch
  val cancellable = main.hostPool.hyperHostsMap.values.map(hh => main.actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, hh.host, CallTrace((x: Host) => x.tick(), "tick")))
}


class Ball(var x : Double, var y : Double, var vx : Double, var vy : Double, var id: String, var clientId: String) extends Element with Observable {
  var energy = 1.0
  val radius = 20

  def collide(ball : Ball)  = {
      val position = Vec(ball.x, ball.y)
      var point = (position + Vec(x,y))*0.5

      if( (position- Vec(x,y)).length() < 2*ball.radius  ) {
        val n2 = (position - point).normalize()
        val v2 = Vec(-n2.y, n2.x)
        val a = ball.vx
        val b = ball.vy
        val speedV = a * Vec(1, 0).dotProd(v2) + b * Vec(0, 1).dotProd(v2)
        val speedN = a * Vec(1, 0).dotProd(n2) + b * Vec(0, 1).dotProd(n2)
        val newSpeed = v2 * speedV - n2 * speedN
        ball.vx = newSpeed.x
        ball.vy = newSpeed.y
    }
  }

}

class Wall(var x : Double, var y : Double, var x2 : Double, var y2 : Double, var id: String, var clientId: String ) extends Element with Observable {
  var frameleft = 60*3
  val v =  Vec(x2-x,y2-y).norm()
  val n = Vec(-v.y, v.x)
  val B = Vec(x,y)
  var shouldDie = false

  var lastH = Vec(0,0)

  def getBH(A : Vec) = {
    val dot = (A-B).dotProd(v)
    v*dot
  }

  def isInSegment(BH : Vec) = {
    BH.dotProd(v) > 0 && BH.length() < (B-Vec(x2,y2)).length()
  }


  def collide(ball : Ball)  = {


    val A = Vec(ball.x,ball.y)
    val BH = getBH(A)
    val H = B + BH
    lastH = H
    val distance = (H-A).length
    if(distance< ball.radius){
      if( isInSegment(BH)  ){
        shouldDie= true
        val a = ball.vx
        val b = ball.vy
        val speedV = a * Vec(1,0).dotProd(v) + b* Vec(0,1).dotProd(v)
        val speedN = a * Vec(1,0).dotProd(n) + b* Vec(0,1).dotProd(n)
        val newSpeed = v*speedV - n*speedN
        ball.vx = newSpeed.x
        ball.vy = newSpeed.y
      }
      else{
        val position = Vec(ball.x, ball.y)
        var point = B
        if( (position- B).length >  (position - Vec(x2,y2)).length )
          point = Vec(x2,y2)
        if( (point- A).length() < ball.radius  ) {
          shouldDie= true
          val n2 = (position - point).normalize()
          val v2 = Vec(-n2.y, n2.x)
          val a = ball.vx
          val b = ball.vy
          val speedV = a * Vec(1, 0).dotProd(v2) + b * Vec(0, 1).dotProd(v2)
          val speedN = a * Vec(1, 0).dotProd(n2) + b * Vec(0, 1).dotProd(n2)
          val newSpeed = v2 * speedV - n2 * speedN
          ball.vx = newSpeed.x
          ball.vy = newSpeed.y
        }
      }
    }
  }


}