package core2

import akka.actor.ActorRef
import core2.CoreMessage.Call

trait Ref[T] {
  val actor : ActorRef
  def call(func: T => Unit) = {
    actor ! Call(func)
  }
}
