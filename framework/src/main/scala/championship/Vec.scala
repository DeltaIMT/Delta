package championship

import core.spatial.Viewable

object Vec  {
  def apply(x: Double, y: Double) = new Vec(x, y)

  def apply() = new Vec(0, 0)
}

class Vec(var x: Double, var y: Double)  extends  Viewable{

  def +(v: Vec) = Vec(x + v.x, y + v.y)

  def unary_- = Vec(-x, -y)

  def -(v: Vec) = this + (-v)

  def *(v: Vec) = Vec(x * v.x, y * v.y)

  def *(mul: Double) = Vec(x * mul, y * mul)

  def /(mul: Double) = Vec(x / mul, y / mul)

  def *=(v: Vec) = {
    x *= v.x
    y *= v.y
  }

  def dotProd(v : Vec) = {
    val t = this*v
    t.x + t.y
  }

  def *=(n: Double): Unit = {
    x *= n
    y *= n
  }

  def /=(n: Double): Unit = {
    x /= n
    y /= n
  }

  def length2() = x * x + y * y

  def length() = math.sqrt(length2())

  def normalize(): Vec = {
    length match {
      case 0 => Vec(0,0)
      case l => this/l
    }
  }

  override def toString: String = "("+x+","+y+")"

}