package game.stgy

object Vec {
  def apply(x: Double, y: Double) = new Vec(x, y)

  def apply() = new Vec(0, 0)
}

class Vec(var x: Double, var y: Double) {

  def +(v: Vec) = Vec(x + v.x, y + v.y)

  def unary_- = Vec(-x, -y)

  def -(v: Vec) = this + (-v)

  def *(v: Vec) = Vec(x * v.x, y * v.y)

  def *=(v: Vec) = {
    x *= v.x
    y *= v.y
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

  def normalize(): Unit = {
    val l = length()
    if (l != 0) {
      x /= l
      y /= l
    }
  }

}