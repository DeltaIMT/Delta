package core

import akka.actor.ActorRef
import core.CoreMessage.Call

trait Ref[T] {
  val actor : ActorRef
  def call(func: T => Unit) = {
    actor ! Call(func)
  }
}
