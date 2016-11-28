package core

import akka.actor.ActorRef
import akka.util.Timeout

import scala.concurrent.duration._
import core.CoreMessage.{Exec, Foreach, Transfert}
import core.user_import.Element

class HyperHost(val host : ActorRef)  {
  implicit val timeout = Timeout(1.second)

  def set(id: String, element: Element) = {
    host ! Set(id,element)
  }

  def foreach(f : (Element) =>  Unit){
    host ! Foreach(f)
  }

  def exec(f : collection.mutable.HashMap[String,Element]=> Unit): Unit = {
    host ! Exec(f)
  }

  def transfert(id: String, element: Element) : Unit = { host  ! Transfert(id, element)}
}
