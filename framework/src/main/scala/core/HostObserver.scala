package core

import akka.actor.ActorRef


abstract class HostObserver(val hostPool: HostPool[_,_]) {

  var container: ActorRef = _

  def setContainer(ar : ActorRef) = {container = ar}
  def self() = container

  def tick() = {}
  def clientInput(id :String ,data: String) = {}

}
