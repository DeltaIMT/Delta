package demo

class Ball(position : Vec, var speed : Vec) extends Vec(position)  {
  def collision(other : Ball): Boolean = (other - this).length() < 40
  def tick = {
    this+=speed
    speed*=0.99
  }
  def toJson= s"""{"x":"${x}","y":"${y}"}"""
}