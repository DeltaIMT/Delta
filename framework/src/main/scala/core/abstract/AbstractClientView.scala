package core.`abstract`

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AnyParts, Disconnect, PlayersUpdate}
import core.user_import.{Element, Zone}
import core.{HostPool, HyperHost}

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

case class Notify(any: Any)

case object UpdateClient

class AbstractClientView(hosts: HostPool, client: ActorRef) extends Actor {

  // USER JOB
  def dataToViewZone(): List[Zone] = Nil

  def onNotify(any: Any): Unit = {}

  def onDisconnect(any: Any): Unit = {}

  def fromListToClientMsg(list: List[Any]): String = list.mkString(",")


  var nextbuffer: Int = 0
  var buffers: mutable.HashMap[Int, (Int, List[Any])] = collection.mutable.HashMap[Int, (Int, List[Any] ) ]()



  def zonesToMessage(zones: List[Zone]): Unit = {

    //println("Zone to view " + zones )
    var hostInsideZones = List[HyperHost]()
    var posFound = List[(Double,Double)]()

    class ElementImpl(var x : Double, var y:Double) extends Element{}
    var fake = new  ElementImpl(0, 0)
    for (x <- 0.0 until (hosts.w * hosts.wn) by hosts.w; y <- 0.0 until (hosts.h * hosts.hn) by hosts.h) {
      var bool = false

      val hostZone = new Zone(x,y,hosts.w, hosts.h)
      zones foreach { z => bool = bool || z.intersectRect( hostZone)  }

      if (bool) {
    //    println("CONTAINS________")

        hostInsideZones = hosts.getHyperHost(x, y) :: hostInsideZones
        posFound = (x,y) :: posFound
      }
    }

    hostInsideZones = hostInsideZones.distinct
//    println("testing host intersected with " + zones(0) + "\nThere is "+ hostInsideZones.size +" host " + posFound.map(h =>  "["+ h._1 + " " + h._2 +"]" ))


   // println("Hosts " + hostInsideZones.size )

    buffers+= nextbuffer -> (hostInsideZones.size, List[Any]())


    val isElementInsideZones = (e: Element) => {
      var bool = false
      zones foreach { z => {
        bool = bool || z.contains(e)
       // println(e + " is in " + z + " ? " + bool)
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
     // println("Any part : destination : "+x.buffer + " content : " + worker.fromListToClientMsg(x.anys))
      var num = x.buffer
      if(nextbuffer - num <= 2) {
        buffers(num)  = (buffers(num)._1-1 ,buffers(num)._2:::x.anys)

        if( buffers(num)._1 == 0) {
          //println("Total parts : " + worker.fromListToClientMsg(buffers(num)._2))
    //      println("Buffer size:"+ buffers.values.size)
          val toDeflate = fromListToClientMsg(buffers(num)._2)


          val arrOutputStream = new ByteArrayOutputStream()
          val zipOutputStream = new GZIPOutputStream(arrOutputStream)
          zipOutputStream.write(toDeflate.getBytes)
          zipOutputStream.close()


          client ! PlayersUpdate(Base64.getEncoder.encodeToString(arrOutputStream.toByteArray))
        }
      }
      else {
       // println("WARNING : Message too old from host to ClientView, delay : " + (nextbuffer - num)  )
      }
    }

    case Disconnect => {

      onDisconnect()

    }


  }


}
