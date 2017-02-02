package core2.clientView

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.AnyParts
import core.`abstract`.{Notify, UpdateClient}
import core2.CoreMessage.{Disconnect, PlayersUpdate}
import core2.HostPool
import core2.host.{Host, HostObserver}
import core2.spatial.Zone
import shapeless.list

import scala.collection.mutable


abstract class ClientView(client: ActorRef) extends Actor {

  var nextbuffer: Int = 0
  var buffers: mutable.HashMap[Int, (Int, List[Any])] = collection.mutable.HashMap[Int, (Int, List[Any])]()

  override def receive: Receive = {
    case x: Notify => {
      onNotify(x.any)
    }
    case UpdateClient => {
      zoneToMessage(dataToViewZone())
      nextbuffer = nextbuffer + 1
      buffers -= nextbuffer - 4
    }

    case x: AnyParts => {
      // println("Any part : destination : "+x.buffer + " content : " + worker.fromListToClientMsg(x.anys))
      var num = x.buffer
      if (nextbuffer - num <= 2) {
        buffers(num) = (buffers(num)._1 - 1, buffers(num)._2 ::: x.anys)

        if (buffers(num)._1 == 0) {
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

  // USER JOB
  def dataToViewZone(): Zone

  def onNotify(any: Any): Unit

  def onDisconnect(any: Any): Unit

  def fromListToClientMsg(list: List[Any]): String

  def zoneToMessage(zone: Zone): Unit = {

    var hostInsideZones = HostPool[Host, HostObserver[_]].hosts.filter { case (z, hr) => z.intersect(zone) }.values
    buffers += nextbuffer -> (hostInsideZones.size, List[Any]())

    val nextbufferCopy = nextbuffer

    hostInsideZones.foreach({
      _.call(

        inside => {
          val res = inside.getViewableFromZone(zone).toList
          self ! AnyParts(nextbufferCopy, res)
        })

    })

  }

}
