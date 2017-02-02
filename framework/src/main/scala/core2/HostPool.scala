package core2


import akka.actor.FSM.->
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import core2.host.{Host, HostRef}
import core2.spatial.{Viewable, Zone}
import kamon.metric.instrument.UnitOfMeasurement.Unknown.U

import scala.concurrent.ExecutionContextExecutor

object HostPool {
    def apply[T,U] = this.asInstanceOf[HostPoolImpl[T,U]]
}


trait HostPoolImpl[HostImpl,HostObsImpl] {

  var hosts =  collection.mutable.HashMap[Zone,HostRef[HostImpl]]()
  var hostsObserver : HostRef[HostObsImpl]

  def addHost(newhosts: List[(Zone, HostRef[HostImpl]) ]) = {
    newhosts foreach {h =>  {
      hosts += h._1 -> h._2
    }}
  }

  def addHostObserver(host: ActorRef) = {
    hostsObserver =  new HostRef[HostObsImpl](host)
  }

  def getHost(viewable: Viewable) : HostRef[HostImpl] = {
   val hostRef = hosts.find { case (zone, hr) => zone.contains(viewable)}
    hostRef match {
      case Some(a) => a._2
      case None => hosts.values.head
    }
  }



  def getHosts(zone: Zone) : Iterable[HostRef[HostImpl]] = {
    val hostRefs = hosts.filter { case (zone, hr) => zone.intersect(zone)}.values
    hostRefs
  }

}

class FakeActor extends Actor{
  override def receive: Receive = ???
}

class FakeHost extends Host{
  override def getViewableFromZone(zones: Iterable[Any]): Iterable[Viewable] = ???

  override def clientInput(id: String, data: String): Unit = ???
}

class FakeHostObserver extends Host{
  override def getViewableFromZone(zones: Iterable[Any]): Iterable[Viewable] = ???

  override def clientInput(id: String, data: String): Unit = ???
}

object test extends App{

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
  val actor = actorSystem.actorOf( Props[FakeActor] )

  val HP = HostPool[FakeHost,FakeHostObserver]
  HP.addHost[FakeHost](IndexedSeq(actor))
 val  tralala = HP.getHost( null)

}