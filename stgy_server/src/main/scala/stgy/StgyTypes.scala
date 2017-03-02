package stgy

object StgyTypes {
  type UnitId=String
  type ClientId= String
  case class Color(r: Int, g: Int, b : Int)
  object UnitType extends Enumeration {
    type UnitType = Value
    val Sword,Bow,Com = Value
  }
}
