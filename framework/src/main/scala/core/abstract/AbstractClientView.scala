package core.`abstract`

import akka.actor.Actor

class AbstractClientView extends Actor {
  override def receive: Receive = {
    case _ => {}
  }
}
