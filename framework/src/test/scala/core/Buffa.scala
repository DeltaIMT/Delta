

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



class Buffalo(var x: Double,var y: Double, var color: Array[Int]) extends Element with Observable {
  var rand = new Random()
  var vx = 0.0
  var vy = 0.0
  var timeout = 100 + rand.nextInt(200)
  var tx = x
  var ty = y
}

class Ball(var x: Double,var y: Double, var color: Array[Int], var id: String, var clientId: String) extends Element with Observable {
  var vx = 0.0
  var vy = 0.0
  var propulx = 0.0
  var propuly = 0.0
  var grapx = 0.0
  var grapy = 0.0
  var grapTx = 0.0
  var grapTy = 0.0
  var grapState = "off"
}

class UserClientView(hostPool: HostPool, client: ActorRef) extends AbstractClientView(hostPool, client) {
  var x = 0.0
  var y = 0.0
  var grapx = 0.0
  var grapy = 0.0
  var idBall = ""

  override def dataToViewZone(): List[Zone] = List(new Zone(x - 1500, y - 1500, 3000, 3000))

  override def onNotify(any: Any): Unit = {

    any match {
      case x: Ball
      => {
        this.x = x.x
        this.y = x.y
        this.grapx = x.grapx
        this.grapy = x.grapy
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

        var stringGrap = ""
        if (grapx != 0 || grapy != 0)
          stringGrap = s""","grap":{"x":"${grapx.toInt}","y":"${grapy.toInt}"}"""

        s"""{"t":"p","x":"${e.x.toInt}","y":"${e.y.toInt}","c":[${e.color(0)},${e.color(1)},${e.color(2)}]${stringGrap}}"""
      }
      case e: Buffalo => {
        s"""{"t":"b","x":"${e.x.toInt}","y":"${e.y.toInt}","c":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case _ => "NOT ELEMENT : " + e
    }) ++ List(s"""{"cam":{"x":"${x.toInt}","y":"${y.toInt}"}}""")
    val string = listString.mkString("[", ",", "]")
    // println(string)
    string
  }
}


class UserHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {
  var rand = new Random()
  var id2ball = mutable.HashMap[String, Ball]()


  var Buffa = new Buffalo(zone.x + rand.nextInt(zone.w.toInt), zone.y + rand.nextInt(zone.h.toInt), Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)))
  elements += "tralala" -> Buffa
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

    var rest = elements.values.filter(x => x.isInstanceOf[Ball]).asInstanceOf[Iterable[Ball]]
    while (rest.nonEmpty) {
      var head = rest.head
      rest = rest.tail
      rest.foreach(other => {
        var x2 = head.x - other.x
        var y2 = head.y - other.y
        if (x2 * x2 + y2 * y2 < 400 * 4) {
          head.color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
          other.color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))

          head.vx += other.propulx
          head.vy += other.propuly
          other.vx += head.propulx
          other.vy += head.propuly
        }
      })
    }

    Buffa.timeout -= 1
    if (Buffa.timeout == 0) {
      Buffa.tx = zone.x + rand.nextInt(zone.w.toInt)
      Buffa.ty = zone.y + rand.nextInt(zone.h.toInt)
      Buffa.timeout = 100 + rand.nextInt(200)
    }
    Buffa.vx = Buffa.tx - Buffa.x
    Buffa.vy = Buffa.ty - Buffa.y
    Buffa.x += Buffa.vx / 50
    Buffa.y += Buffa.vy / 50

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

          var x2 = e.x - Buffa.x
          var y2 = e.y - Buffa.y
          if (x2 * x2 + y2 * y2 < math.pow(20 + 60, 2)) {

            e.x = rand.nextInt(3000)
            e.y = rand.nextInt(3000)
          }

          if (e.grapState == "deploying") {
            e.grapx += e.grapTx * 8
            e.grapy += e.grapTy * 8
            if (e.grapx * e.grapx + e.grapy * e.grapy > 300 * 300)
              e.grapState = "retracting"
          }
          else if (e.grapState == "retracting") {
            e.grapx *= 0.90
            e.grapy *= 0.90
            if (math.abs(e.grapx) < 1) e.grapx = 0
            if (math.abs(e.grapy) < 1) e.grapy = 0

            if (e.grapx == 0 && e.grapy == 0) e.grapState = "off"
          }
        }
        case _ => {}
      }
    }
  }

  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]
    val bool = (json \ "cl").get.as[Boolean]
    val rbool = (json \ "cr").get.as[Boolean]
    if (id2ball.contains(id)) {
      val b = id2ball(id)

      var toMouseX = x - b.x
      var toMouseY = y - b.y

      var l = math.sqrt(toMouseX * toMouseX + toMouseY * toMouseY)
      if (l != 0) {
        toMouseX /= l
        toMouseY /= l
      }
      else {
        toMouseX = 0
        toMouseY = 0
      }
      var vx = toMouseX * 10
      var vy = toMouseY * 10
      if (l < 20) {
        vx *= l / 20
        vy *= l / 20
      }

      b.vx = b.vx * 0.98 + vx * 0.02
      b.vy = b.vy * 0.98 + vy * 0.02

      if (bool) {
        b.propulx = 1 * (x - b.x) / l;
        b.propuly = 1 * (y - b.y) / l;
      }

      if (rbool && b.grapState == "off") {
        println("Grappin !!!")
        b.grapState = "deploying"
        b.grapTx = toMouseX
        b.grapTy = toMouseY
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


class Buffa extends FunSuite {

  test("Buffa") {

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
