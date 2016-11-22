package core

import akka.actor.ActorRef
import akka.dispatch.Foreach
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._
import core.CoreMessage.{GetList, GetListFilter}
import core.user_import.Element

import scala.collection.mutable
import scala.concurrent.Future

class HyperHost(val host : ActorRef)  {
  implicit val timeout = Timeout(1.second)

  def set(id: String, element: Element) = {
    host ! Set(id,element)
  }

  def foreach(f : (Element) =>  Unit){
    host ! Foreach(f)
  }

  def foreach(f : (Element) =>  Unit, filter : (Element) => Boolean){
    host ! Foreach(f,filter)
  }

}
