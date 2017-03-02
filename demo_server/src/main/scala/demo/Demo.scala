package demo

import core.Delta
import core.host.HostPool

object Demo extends App{
  val HP = HostPool[DemoHost, Nothing]
  val delta = new Delta[DemoHost, DemoProvider, Nothing]()
  val hosts = for(x <- 0 to 3; y <- 0 to 3) yield {
    val zone= new SquareZone(x*200,y*200,200,200)
    new DemoHost(zone)
  }
  delta.launch(hosts)
  HP.hosts.values.foreach( hr =>  delta.setHostInterval(hr,16, h=> h.tick) )
}







