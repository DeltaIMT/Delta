package core.`abstract`

import akka.actor.{Actor, ActorRef}
import core.HostPool

case class Notify(any : Any)
case object UpdateClient
case class Zone(x :Double, y: Double, w : Double, h : Double)


abstract class AbstractClientView(hosts: HostPool,client : ActorRef) extends Actor {

  // USER JOB
  def dataToViewZone() : List[Zone] = ???
  def onNotify(any: Any) :Unit = ???
  def fromListToClientMsg(list : List[Any])= ???

  // TODO HERE CONVERTION FROM ZONE TO LIST OF ELEMENT CONTAINED IN THE ZONES BY ASKING HOSTS
  def zonesToList(zones : List[Zone]) :  List[Any] = {

    val hostInsideZones = zones map {z => hosts.getHyperHost(z.x,z.y)}
    hostInsideZones foreach {h =>  h ? getList() }

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
