package core

import akka.actor.ActorRef
import core.user_import.{Element, Zone}

abstract class Host(val hostPool: HostPool[_,_], val zone: Zone) {
  var container: ActorRef = _
  def setContainer(ar : ActorRef) = {container = ar}
  def self() = container

  var elements = collection.mutable.HashMap[String,Element]()


  def tick() = {}
  def clientInput(id :String ,data: String) = {}
  def getNum = (zone.x.toInt/zone.w).toInt + "-" + (zone.y.toInt/zone.w).toInt


}
