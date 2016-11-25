import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.stream.ActorMaterializer
import core.CoreMessage.{Tick, Transfert}
import core.{FakeClient, HostPool, Provider, Websocket}
import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost, UpdateClient}
import core.user_import.{Element, Zone}
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AbstractHostTest extends FunSuite {



  //defining some user classes needed to compile
  class UserClientView(hosts: HostPool, client: ActorRef) extends AbstractClientView(hosts, client) {
    override def dataToViewZone(): List[Zone] = List(new Zone(0, 0, 300, 300))

    override def onNotify(any: Any): Unit = println(any)

    override def fromListToClientMsg(list: List[Any]) = {
      val string = list.map(e => e match {
        case e: Element => {
          "(" + e.x + "," + e.y + ")"
        }
        case _ => "NOTELEMENT : " + e
      }).mkString(",")
      //println(string)
      string
    }
  }

  class UserHost(hostPool: HostPool, i : Int) extends AbstractHost(hostPool) {

    if (i==0){
      elements += "1" -> new Element(0, 0)
    }

    override def tick(): Unit = {
      if (i==0 && elements.values.size > 0) {
        elements.values.head.x = elements("1").x + 20
        var e = elements("1")
        println("(" + e.x + "," + e.y + ")")

        if (e.x > 200)
          {
            hostPool.getHyperHost(e.x,e.y).host ! Transfert("1", e)
            elements -= "1"
          }

      }

      if(i==1){


        println(elements.size)

      }


    }

  }

  class UserSpecialHost extends AbstractSpecialHost(null) {

  }


  test("AbstractClientViewTest") {

    println("framework starting")
    implicit val actorSystem = ActorSystem("akka-system")
    implicit val flowMaterializer = ActorMaterializer()
    val initialPort = 5000
    val numberOfClient = 100
    val hostsGridWidth = 5
    val hostsGridHeight = 5
    val hostWidth = 200
    val hostHeight = 200

    val hostPool = new HostPool(hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
    val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => actorSystem.actorOf(Props(new UserHost(hostPool,i)), "host" + i) }
    hostPool.addHost(hosts)
    val specialHost = actorSystem.actorOf(Props(new UserSpecialHost()), "specialHost")

    var cancellable = hosts map {h => actorSystem.scheduler.schedule(1000 milliseconds, 1000 milliseconds,h ,Tick )}



    println("framework working")

    Thread.sleep(100000)
    println("framework shutting down")
    cancellable foreach { c => c.cancel()}
    actorSystem.terminate()
    println("framework down")
  }



}
