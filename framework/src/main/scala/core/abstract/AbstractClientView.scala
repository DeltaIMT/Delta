package core.`abstract`

import akka.actor.FSM.->
import akka.pattern._
import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AnyParts, PlayersUpdate}
import core.{HostPool, HyperHost}
import core.user_import.{Element, Zone}

import scala.collection.mutable

case class Notify(any: Any)

case object UpdateClient


class AbstractClientView(hosts: HostPool, client: ActorRef) extends Actor {

  var nextbuffer: Int = 0
  var buffers: mutable.HashMap[Int, (Int, List[Any])] = collection.mutable.HashMap[Int, (Int, List[Any] ) ]()

  // USER JOB
  def dataToViewZone(): List[Zone] = Nil

  def onNotify(any: Any): Unit = {}

  def fromListToClientMsg(list: List[Any]): String = list.mkString(",")

  def zonesToMessage(zones: List[Zone]): Unit = {

    var hostInsideZones = List[HyperHost]()

    var fake = new Element(0, 0)
    for (x <- 0.0 until hosts.w * hosts.wn by hosts.w; y <- 0.0 until hosts.h * hosts.hn by hosts.h) {
      fake.x = x
      fake.y = y
   //   println("x: " + x)
   //   println("y: " + y)
      var bool = false
      zones foreach { z => bool = bool || z.contains(fake) }

      if (bool) {
    //    println("CONTAINS________")
        hostInsideZones = hosts.getHyperHost(x, y) :: hostInsideZones
      }
    }

    hostInsideZones = hostInsideZones.distinct


    buffers+= nextbuffer -> (hostInsideZones.size, List[Any]())


    val isElementInsideZones = (e: Element) => {
      var bool = false
      zones foreach { z => {
        bool = bool || z.contains(e)
     //   println(e + " is in " + z + " ? " + bool)
      }
      }
      bool
    }


  //  println("sending to buffer :" + nextbuffer)
    val nextbufferCopy = nextbuffer

    hostInsideZones.foreach({ h => {
      h.exec(
        l => {
          val res = l.values.filter(isElementInsideZones).toList
       //   println("SIZE: " + res.size + " sending to buffer :" + nextbufferCopy)
          self ! AnyParts(nextbufferCopy, res)
        }
      )

    }
    })

  }

  override def receive: Receive = {
    case x: Notify => {
      onNotify(x.any)
    }
    case UpdateClient => {
     zonesToMessage(dataToViewZone())
      nextbuffer = nextbuffer+1
      buffers -= nextbuffer-4
    }

    case x: AnyParts => {
     // println("Any part : destination : "+x.buffer + " content : " + fromListToClientMsg(x.anys))
      var num = x.buffer
      if(nextbuffer - num <= 2) {
        buffers(num)  = (buffers(num)._1-1 ,buffers(num)._2:::x.anys)

        if( buffers(num)._1 == 0) {
          //println("Total parts : " + fromListToClientMsg(buffers(num)._2))
    //      println("Buffer size:"+ buffers.values.size)
          client ! PlayersUpdate(fromListToClientMsg(buffers(num)._2))
        }
      }
      else {
        println("WARNING : Message too old from host to ClientView, delay : " + (nextbuffer - num)  )
      }
    }


  }


}
