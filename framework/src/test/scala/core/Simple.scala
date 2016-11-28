package core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.stream.ActorMaterializer
import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost}
import core.port_dispatch.ProviderPort
import core.script_filled.{UserHost, UserSpecialHost}
import core.user_import.{Element, Zone}
import org.scalatest.FunSuite

class Simple extends FunSuite  {

  //defining some user classes needed to compile
  class UserClientView(hosts: HostPool, client: ActorRef) extends AbstractClientView(hosts, client) {
    override def dataToViewZone(): List[Zone] = List(new Zone(0, 0, 300, 300))

    override def onNotify(any: Any): Unit = println(any)

    override def fromListToClientMsg(list: List[Any]) = {
      val string = list.map(e => e match {
        case e: Element => {
          "(" + e.x + "," + e.y + ")"
        }
        case _ => "NOT ELEMENT : " + e
      }).mkString(",")
      string
    }
  }

  class UserHost(hostPool: HostPool) extends AbstractHost(hostPool) {

    elements += "1" -> new Element(1, 2)

  }

  class UserSpecialHost(hostPool: HostPool extends AbstractSpecialHost(hostPool) {

  }


  test("SimpleApp"){


    println("framework starting")
    implicit val actorSystem = ActorSystem("akka-system")
    implicit val flowMaterializer = ActorMaterializer()
    val initialPort = 9001
    val numberOfClient = 100
    val hostsGridWidth = 5
    val hostsGridHeight = 5
    val hostWidth = 200
    val hostHeight = 200

    val hostPool = new HostPool(hostWidth,hostHeight,hostsGridWidth,hostsGridHeight)
    val hosts =  0 until hostsGridWidth*hostsGridHeight map {i=> actorSystem.actorOf(Props(new UserHost(hostPool)),"host_"+i)}
    hostPool.addHost(hosts)
    val specialHost = actorSystem.actorOf(Props(new UserSpecialHost(hostPool)), "specialHost")

    val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient)),"providerPort")
    val providerClients = 0 until numberOfClient map {i=>actorSystem.actorOf(Props(new Provider(hostPool, specialHost)),"provider_"+i)}
    val providers = providerPort :: providerClients.toList
    val websockets = -1 until numberOfClient map {i=>initialPort+i -> new Websocket(providers(i+1),initialPort+i)}

    val routes = websockets.map(x => {
      x._1 ->
        (get & parameter("id") ){
          id =>  handleWebSocketMessages(x._2.flow(id, "region"))
        }
    })

    val interface ="localhost"

    routes foreach { route =>
      Http().bindAndHandle(route._2, "0.0.0.0", route._1)
    }
    println("framework working")

    Thread.sleep(1000000)
    println("framework shutdownn")
    actorSystem.terminate()


  }


}
