package core.`abstract`

import akka.pattern._
import akka.actor.{Actor, ActorRef}
import core.HostPool
import core.user_import.{Element, Zone}

case class Notify(any : Any)
case object UpdateClient


abstract class AbstractClientView(hosts: HostPool,client : ActorRef) extends Actor {

  // USER JOB
  def dataToViewZone() : List[Zone] = ???
  def onNotify(any: Any) :Unit = ???
  def fromListToClientMsg(list : List[Any])= ???

  // TODO HERE CONVERTION FROM ZONE TO LIST OF ELEMENT CONTAINED IN THE ZONES BY ASKING HOSTS
  def zonesToList(zones : List[Zone]) :  List[Any] = {

    var hostInsideZones = zones map {z => hosts.getHyperHost(z.x,z.y)}
    hostInsideZones = hostInsideZones.distinct
    var res = List[Element]()
    hostInsideZones foreach { h =>

      res = res :::  h.getList( (e : Element) =>  {
                      var bool = false
                      zones foreach { z => bool = bool || z.contains(e)}
                      bool
                    })
    }
    res
  }

  override def receive: Receive = {
    case x : Notify => {
      onNotify(x.any)
    }
    case UpdateClient => {
      var list =zonesToList(dataToViewZone())
      client ! fromListToClientMsg(list)
    }

  }



}
