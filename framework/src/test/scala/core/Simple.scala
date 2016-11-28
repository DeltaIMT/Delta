package core

import java.time.ZoneId

import akka.actor.FSM.->
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.stream.ActorMaterializer
import core.CoreMessage.{Tick, Transfert}
import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost}
import core.port_dispatch.ProviderPort
import core.script_filled.UserClientView
import core.user_import.{Element, Observable, Zone}
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


class UserClientView(hostPool: HostPool, client: ActorRef) extends AbstractClientView(hostPool, client) {
  var x = 0.0
  var y = 0.0

  override def dataToViewZone(): List[Zone] = List(new Zone(x - 1000, y - 1000, x + 1000, y + 1000))

  override def onNotify(any: Any): Unit = {

    any match {

      case x: Ball => {
        this.x = x.x
        this.y = x.y
      }

      case _ => {
        println("lol")
      }

    }
  }

  override def fromListToClientMsg(list: List[Any]) = {
    val listString = list.map(e => e match {
      case e: Element => {
        s"""{"x":"${e.x}","y":"${e.y}"}"""
      }
      case _ => "NOT ELEMENT : " + e
    }) ++ List(s"""{"cam":{"x":"${x}","y":"${y}"}}""")
    val string = listString.mkString("[",",","]")
    println(string)
    string
  }

}


class UserHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {

  override def tick(): Unit = {

    elements foreach { elem =>
      elem._2 match {
        case e: Ball => {

          e.x += 1
          e.notifyClientViews

          if (!zone.contains(e)) {

            println("Il faut sortir de " + zone.x + " " + zone.y)
            hostPool.getHyperHost(e.x, e.y).transfert(elem._1, e)
            elements -= elem._1
          }

        }
      }
    }
  }

}

class UserSpecialHost(hostPool: HostPool) extends AbstractSpecialHost[UserClientView](hostPool) {

  var rand = new Random()

  override def OnConnect(client: ActorRef) = {

    val b = new Ball(rand.nextInt(100), rand.nextInt(1000), Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)))
    val hyper = hostPool.getHyperHost(b.x, b.y)
    hyper.exec(hm => hm += rand.nextString(20) -> b)
    b.sub(client)
  }

}


class Ball(x: Double, y: Double, var color: Array[Int]) extends Element(x, y) with Observable {

}


class Simple extends FunSuite {

  test("SimpleApp") {

    println("framework starting")
    implicit val actorSystem = ActorSystem("akka-system")
    implicit val flowMaterializer = ActorMaterializer()
    val initialPort = 9001
    val numberOfClient = 100
    val hostsGridWidth = 5
    val hostsGridHeight = 5
    val hostWidth = 600
    val hostHeight = 600

    val hostPool = new HostPool(hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
    val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => actorSystem.actorOf(Props(new UserHost(hostPool, new Zone(hostPool.fromI2X(i) * hostWidth, hostPool.fromI2Y(i) * hostHeight, hostWidth, hostHeight))), "host_" + i) }
    hostPool.addHost(hosts)
    val specialHost = actorSystem.actorOf(Props(new UserSpecialHost(hostPool)), "specialHost")

    val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient)), "providerPort")
    val providerClients = 0 until numberOfClient map { i => actorSystem.actorOf(Props(new Provider(hostPool, specialHost)), "provider_" + i) }
    val providers = providerPort :: providerClients.toList
    val websockets = -1 until numberOfClient map { i => initialPort + i -> new Websocket(providers(i + 1), initialPort + i) }

    val routes = websockets.map(x => {
      x._1 ->
        (get & parameter("id")) {
          id => handleWebSocketMessages(x._2.flow(id, "region"))
        }
    })

    val interface = "localhost"

    routes foreach { route =>
      Http().bindAndHandle(route._2, "0.0.0.0", route._1)
    }

    var cancellable = hosts map { h => actorSystem.scheduler.schedule(1000 milliseconds, 33 milliseconds, h, Tick) }

    println("framework working")

    Thread.sleep(1000000)
    println("framework shutdownn")
    cancellable foreach { c => c.cancel() }
    actorSystem.terminate()

  }

}
