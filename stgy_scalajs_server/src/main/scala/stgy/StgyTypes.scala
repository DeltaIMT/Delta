package stgy

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

object StgyTypes {
  type UnitId=String
  type ClientId= String
  case class Color(r: Int, g: Int, b : Int) {
    def apply() = s"""rgb(${r},${g},${b})"""
  }

  object UnitType extends Enumeration {
    type UnitType = Value
    val Sword,Bow,Com = Value
  }
}
