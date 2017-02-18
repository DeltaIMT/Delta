package demo

import core.AbstractMain
import core.host.HostPool

object Demo extends App{

  val HP = HostPool[DemoHost, DemoHostObserver]
  val main = new AbstractMain[DemoHost, DemoProvider, DemoHostObserver]()
  main.numberOfClient = 100

  val hosts =(0 until 25).map {i => {
    val x = (i %5)*600
    val y = (i /5)*600
    val zone= new SquareZone(x,y,600,600)
    new DemoHost(zone, i)
  }}


  val hostObserver = new DemoHostObserver
  main.launch(hosts, hostObserver)

  main.HP.hosts.values.foreach( hr =>  main.setHostInterval(hr,16, h=> h.tick) )
}
