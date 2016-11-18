package core.`abstract`

import akka.actor.{Actor, ActorRef}

case class Notify(any : Any)
case object UpdateClient
case class Zone(x :Double, y: Double, w : Double, h : Double)


abstract class AbstractClientView(hosts: IndexedSeq[ActorRef],client : ActorRef) extends Actor {

  // USER JOB
  def dataToViewZone() : List[Zone] = ???
  def onNotify(any: Any) :Unit = ???
  def fromListToClientMsg(list : List[Any])= ???

  // TODO HERE CONVERTION FROM ZONE TO LIST OF ELEMENT CONTAINED IN THE ZONES BY ASKING HOSTS
  def zonesToList(zones : List[Zone]) :  List[Any] = {

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
