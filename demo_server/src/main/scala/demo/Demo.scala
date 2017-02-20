package demo

import core.Delta
import core.host.HostPool
import core.observerPattern.Observable

object Demo extends App{
//  val HP = HostPool[DemoHost, Nothing]
//  val delta = new Delta[DemoHost, DemoProvider, Nothing]()
//  val hosts = for(x <- 0 to 3; y <- 0 to 3) yield {
//    val zone= new SquareZone(x*200,y*200,200,200)
//    new DemoHost(zone)
//  }
//  delta.launch(hosts)
//  HP.hosts.values.foreach( hr =>  delta.setHostInterval(hr,16, h=> h.tick) )
}







/*
package demo

import core.Delta
import core.host.HostPool
import core.observerPattern.Observable

class Ball(position : Vec, var speed : Vec) extends Vec(position)  {
  def collision(other : Ball): Boolean = (other - this).length() < 40
  def tick = {
    this+=speed
    speed*=0.99
  }
  def toJson= s"""{"x":"${x}","y":"${y}"}"""
}

object Demo extends App{
  val HP = HostPool[DemoHost, Nothing]
  val delta = new Delta[DemoHost, DemoProvider, Nothing]()
  val hosts = for(x <- 0 to 3; y <- 0 to 3) yield {
    val zone= new SquareZone(x*300,y*300,300,300)
    new DemoHost(zone)
  }
  delta.launch(hosts)
  HP.hosts.values.foreach( hr =>  delta.setHostInterval(hr,16, h=> h.tick) )
}

*/