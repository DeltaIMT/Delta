package core.`abstract`

import akka.actor.Actor

class AbstractSpecialHost extends Actor{
  override def receive: Receive = {
    case _ => {}
  }
}
