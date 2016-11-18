package core.`abstract`

import akka.pattern._
import akka.actor.{Actor, ActorRef}
import core.{HostPool, HyperHost}
import core.user_import.{Element, Zone}

case class Notify(any: Any)

case object UpdateClient


abstract class AbstractClientView(hosts: HostPool, client: ActorRef) extends Actor {

  // USER JOB
  def dataToViewZone(): List[Zone] = ???

  def onNotify(any: Any): Unit = ???

  def fromListToClientMsg(list: List[Any]) = ???

  def zonesToList(zones: List[Zone]): List[Any] = {

    var hostInsideZones = List[HyperHost]()

    var fake = new Element(0,0)
    for( x <- 0 until hosts.w*hosts.wn by hosts.w ; y <- 0 until hosts.h*hosts.hn by hosts.h ){
      fake.x=x
      fake.y=y

      var bool = false
      zones foreach { z => bool = bool || z.contains(fake) }

      if (bool)
      {
          hostInsideZones =   hosts.getHyperHost(x, y) :: hostInsideZones
      }
    }

    hostInsideZones = hostInsideZones.distinct
    var res = List[Element]()
    hostInsideZones foreach { h =>
      res = res ::: h.getListFilter(
        (e: Element) => {
          var bool = false
          zones foreach { z => bool = bool || z.contains(e) }
          bool
        }
      )
    }
    res
  }

  override def receive: Receive = {
    case x: Notify => {
      onNotify(x.any)
    }
    case UpdateClient => {
      var list = zonesToList(dataToViewZone())
      client ! fromListToClientMsg(list)
    }

  }


}
