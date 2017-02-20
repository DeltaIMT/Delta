package demo

import play.api.libs.json.Json

object jason {

  def getJ4[T1 <: AnyVal,T2 <: AnyVal,T3 <: AnyVal,T4 <: AnyVal](data: String, param1: String, param2: String, param3: String, param4: String): (T1,T2,T3,T4) = {
    val json = Json.parse(data)
    val x = (json \ param1).get.as[T1]
    val y = (json \ param2).get.as[T2]
    val tx = (json \ param3).get.as[T3]
    val ty = (json \ param4).get.as[T4]
    (x,y,tx,ty)
  }

  def getJ3[T1 <: AnyVal,T2 <: AnyVal,T3 <: AnyVal](data: String, param1: String, param2: String, param3: String): (T1,T2,T3) = {
    val json = Json.parse(data)
    val x = (json \ param1).get.as[T1]
    val y = (json \ param2).get.as[T2]
    val tx = (json \ param3).get.as[T3]
    (x,y,tx)
  }

  def getJ2[T1 <: AnyVal,T2 <: AnyVal](data: String, param1: String, param2: String): (T1,T2) = {
    val json = Json.parse(data)
    val x = (json \ param1).get.as[T1]
    val y = (json \ param2).get.as[T2]
    (x,y)
  }

  def getJ1[T1 <: AnyVal](data: String, param1: String): T1 = {
    val json = Json.parse(data)
    val x = (json \ param1).get.as[T1]
    x
  }

}
