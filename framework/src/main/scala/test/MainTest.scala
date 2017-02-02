package test

import core2.AbstractMain

object MainTest extends App{
  val main = new AbstractMain[HostTest,ProviderTest,HostObsTest]()
  val hosts = (0 until 3).map( i => new HostTest(new SquareZone(i*100,0,100,100)))
  val hostObs = new HostObsTest()
  hosts.head.list = List(new ViewableTest(0,0))
  main.launch(hosts,hostObs )
  val hr= main.HP.hosts.values
  hr.foreach( hr => main.setInterval(hr, 100, h => h.tick))
}
