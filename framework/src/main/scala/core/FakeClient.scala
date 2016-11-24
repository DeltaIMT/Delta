package core

import akka.actor.Actor
import akka.actor.Actor.Receive
import core.CoreMessage.PlayersUpdate

class FakeClient extends Actor {
  override def receive: Receive = {

    case x : PlayersUpdate => println("Client : "+x.json)

  }
}
