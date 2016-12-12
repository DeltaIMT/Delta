

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.Tick
import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost}
import core.port_dispatch.ProviderPort
import core.user_import.{Element, Observable, Observer, Zone}
import core.{HostPool, Provider, Websocket}
import org.scalatest.FunSuite
import play.api.libs.json.Json

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Random

class Ball(x: Double, y: Double, var color: Array[Int], var id: String, var clientId: String) extends Element(x, y) with Observable {
  var vx = 0.0
  var vy = 0.0
}

class UserClientView(hostPool: HostPool, client: ActorRef) extends AbstractClientView(hostPool, client) {
  var x = 0.0
  var y = 0.0
  var idBall = ""

  override def dataToViewZone(): List[Zone] = List(new Zone(x - 1000, y - 1000, 2000, 2000))

  override def onNotify(any: Any): Unit = {

    any match {
      case x: Ball
      => {
        this.x = x.x
        this.y = x.y
        this.idBall = x.id
      }
      case _ => {
        println("lol")
      }
    }
  }

  override def onDisconnect(any: Any): Unit = {
    hostPool.getHyperHost(x, y).exec(l => l -= idBall)
  }

  override def fromListToClientMsg(list: List[Any]) = {
    val listString = list.map(e => e match {
      case e: Ball => {
        s"""{"x":"${e.x.toInt}","y":"${e.y.toInt}","c":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case _ => "NOT ELEMENT : " + e
    }) ++ List(s"""{"cam":{"x":"${x.toInt}","y":"${y.toInt}"}}""")
    val string = listString.mkString("[", ",", "]")
    // println(string)
    string
  }
}


class UserHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {

  var id2ball = mutable.HashMap[String, Ball]()
  var rand = new Random()

  methods += "addBall" -> ((arg: Any) => {
    var e = arg.asInstanceOf[Ball]
    id2ball += e.clientId -> e
  })

  methods += "transfert" -> ((arg: Any) => {
    var seq = arg.asInstanceOf[Seq[Any]]
    var idObject = seq(0).asInstanceOf[String]
    var e = seq(1).asInstanceOf[Ball]
    var idClient = seq(2).asInstanceOf[String]
    hostPool.getHyperHost(e.x, e.y).transfert(idObject, e)
    id2ball += idClient -> e
  })

  override def tick(): Unit = {

    var rest = elements.values.filter( x => x.isInstanceOf[Ball] ).asInstanceOf[Iterable[Ball]]
    while(rest.nonEmpty){
      var head = rest.head
      rest = rest.tail
      rest.foreach( other => {
        var x2 = head.x-other.x
        var y2 = head.y-other.y
        if (  x2*x2 + y2*y2 < 400*4   ){
          head.color= Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
          other.color= Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
        }
      })
    }

    elements foreach { elem =>
      elem._2 match {
        case e: Ball => {
          e.x += e.vx
          e.y += e.vy
          e.notifyClientViews
          if (!zone.contains(e)) {
       //     println("Il faut sortir de " + zone.x + " " + zone.y)
            hostPool.getHyperHost(e.x, e.y).method("transfert", Seq(elem._1, e, e.clientId))
            id2ball -= e.clientId
            elements -= elem._1
          }
        }
      }
    }
  }

  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]
    if (id2ball.contains(id)) {
      val b = id2ball(id)
      b.vx = (x - b.x)
      b.vy = (y - b.y)
      var l  = math.sqrt(b.vx*b.vx + b.vy * b.vy)

      if(l  != 0) {
        b.vx /= l
        b.vy /= l
      }

      b.vx *= 10
      b.vy *= 10

      if (l < 20 ){
        b.vx *=l/20
        b.vy *=l/20
      }


    }
  }
}

class UserSpecialHost(hostPool: HostPool) extends AbstractSpecialHost[UserClientView](hostPool) {

  var rand = new Random()

  override def OnConnect(id: String, obs: Observer) = {

    val randId = rand.nextString(20)
    //  println(randId)
    val b = new Ball(rand.nextInt(100), rand.nextInt(1000), Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), randId, id)
    val hyper = hostPool.getHyperHost(b.x, b.y)
    b.sub(obs)
 //   println("Calling adding ball")
    hyper.exec(hm => {
 //     println("Adding ball");
      hm += randId -> b
    })
    hyper.method("addBall", b)
  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }
}


class Simple extends FunSuite {

  test("SimpleApp") {

    println("framework starting")
    implicit val actorSystem = ActorSystem("akka-system")
    implicit val executionContext = actorSystem.dispatcher
    implicit val flowMaterializer = ActorMaterializer()
    val initialPort = 9001
    val numberOfClient = 500
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
      Http().bindAndHandle(RouteResult.route2HandlerFlow(route._2), "0.0.0.0", route._1)
    }

    var cancellable = hosts map { h => actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, h, Tick) }

    println("framework working")

    Thread.sleep(1000000)
    println("framework shutdownn")
    cancellable foreach { c => c.cancel() }
    actorSystem.terminate()

  }

}
