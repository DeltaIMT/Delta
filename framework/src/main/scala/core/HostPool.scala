package core

import akka.actor.ActorRef

class HostPool(val w : Double,val h: Double, val wn :Int, val hn : Int) {

  var hyperHostsMap =  collection.mutable.HashMap[ActorRef,HyperHost]()
  var hosts  = IndexedSeq[ActorRef]()
  def addHost(hosts: IndexedSeq[ActorRef]) = {
    this.hosts = hosts
    hosts foreach {h =>hyperHostsMap += h -> new HyperHost(h)}
  }

  def clamp(i : Int ) = math.min(math.max(i,0), wn*hn-1  )

  def fromXY2I(x : Double, y  :Double) : Int =  clamp( wn* (y/h).toInt + (x/w).toInt)
  def fromXY2I(x : Int, y  :Int) : Int = clamp (wn*y + x)
  def fromI2X(i : Int): Int  = i % wn
  def fromI2Y(i : Int): Int  = i / wn

  def getHyperHost(i : Int) = hyperHostsMap(hosts(i))
  def getHyperHost(x : Int, y : Int ): HyperHost =  getHyperHost( fromXY2I(x,y))
  def getHyperHost(x : Double, y : Double ): HyperHost =  getHyperHost( fromXY2I(x,y))

}
