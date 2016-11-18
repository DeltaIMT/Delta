package core.`abstract`

import akka.actor.Actor

abstract class AbstractHost extends Actor {
  override def receive: Receive = {
    case _ => {}
  }
}
