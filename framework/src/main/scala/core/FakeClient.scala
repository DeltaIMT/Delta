package core

import akka.actor.Actor
import akka.actor.Actor.Receive

class FakeClient extends Actor {
  override def receive: Receive = {

    case x : String => println(x)

  }
}
