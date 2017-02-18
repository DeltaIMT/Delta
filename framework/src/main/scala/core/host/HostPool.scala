package core.host

import core.spatial.{Viewable, Zone}


//Object that contains that knows the references to all the hosts
object HostPool extends HostPoolImpl[Nothing,Nothing] {
    def apply[T <: InputReceiver,U <: InputReceiver] = this.asInstanceOf[HostPoolImpl[T,U]]
}


trait HostPoolImpl[HostImpl <: InputReceiver,HostObsImpl <: InputReceiver] {

  var hosts =  collection.mutable.HashMap[Zone,HostRef[HostImpl]]()
  var hostObserver : HostRef[HostObsImpl]= _

  def getHost(viewable: Viewable) : HostRef[HostImpl] = {
   val hostRef = hosts.find { case (zone, hr) => zone.contains(viewable)}
    hostRef match {
      case Some(a) => a._2
      case None => hosts.values.head
    }
  }

  def getHosts(zone: Zone) : Iterable[HostRef[HostImpl]] = {
    val hostRefs = hosts.filter { case (zone2, hr) => zone.intersect(zone2) }.values
    hostRefs
  }

}
