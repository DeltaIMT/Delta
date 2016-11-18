package core

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive

class Provider(hosts: IndexedSeq[ActorRef]) extends Actor{
  override def receive: Receive = {
    case _ => {}
  }
}
