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

import scala.concurrent.duration._
import scala.util.Random


object Vec {
  def apply(x: Double, y: Double) = new Vec(x, y)

  def apply() = new Vec(0, 0)
}

class Vec(var x: Double, var y: Double) {

  def +(v: Vec) = Vec(x + v.x, y + v.y)

  def unary_- = Vec(-x, -y)

  def -(v: Vec) = this + (-v)

  def *(v: Vec) = Vec(x * v.x, y * v.y)

  def *=(v: Vec) = {
    x *= v.x
    y *= v.y
  }

  def *=(n: Double): Unit = {
    x *= n
    y *= n
  }

  def /=(n: Double): Unit = {
    x /= n
    y /= n
  }

  def length2() = x * x + y * y

  def length() = math.sqrt(length2())

  def normalize(): Unit = {
    val l = length()
    if (l != 0) {
      x /= l
      y /= l
    }
  }

}

abstract class Unity(x: Double, y: Double, var id: String, var clientId: String, var color: Array[Int]) extends Element(x, y) with Observable {
}

trait Movable {
  var move = false
  var target = Vec()
  var speed = 5
}

trait Damagable {
  var health = 1.0
}

class Bowman(x: Double, y: Double, id: String, clientId: String, color: Array[Int]) extends Unity(x, y, id, clientId, color) with Movable with Damagable {

}

class Flag(x: Double, y: Double, id: String, clientId: String, color: Array[Int]) extends Unity(x, y, id, clientId, color) {

}


class StgyClientView(hostPool: HostPool, client: ActorRef) extends AbstractClientView(hostPool, client) {
  var pos = Vec(1500, 1500)
  var id = ""

  override def dataToViewZone(): List[Zone] = List(new Zone(pos.x - 1500, pos.y - 1500, 3000, 3000))

  override def onNotify(any: Any): Unit = {

    any match {
      case e: IdGiver => id = e.id
      case bowman: Bowman => {
        //println("notify not matched")
      }
      case _ => {
        println("notify not matched")
      }
    }
  }

  override def onDisconnect(any: Any): Unit = {
    //  hostPool.getHyperHost(x, y).exec(l => l -= idBall)
  }

  override def fromListToClientMsg(list: List[Any]) = {
    val listString = list.map(e => e match {

      case e: Bowman => {
        s"""{"type":"bowman","id":"${e.id}","mine":${id == e.clientId},"health":"${e.health}","x":"${e.x.toInt}","y":"${e.y.toInt}","color":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case _ => "NOT ELEMENT : " + e
    }) ++ List( s"""{"type":"camera","x":"${pos.x.toInt}","y":"${pos.y.toInt}"}""")
    val string = listString.mkString("[", ",", "]")
    // println(string)
    string
  }
}


class StgyHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {
  var rand = new Random()

  methods += "addBowman" -> ((arg: Any) => {
    var e = arg.asInstanceOf[Bowman]
    elements += e.id -> e
  })

  override def tick(): Unit = {


    var rest = elements.values.filter(x => x.isInstanceOf[Bowman]).asInstanceOf[Iterable[Bowman]]
    while (rest.nonEmpty) {
      var head = rest.head
      rest = rest.tail
      rest.foreach(other => {
        var x2 = head.x - other.x
        var y2 = head.y - other.y
        if (head.clientId!= other.clientId &&  x2 * x2 + y2 * y2 < 400 * 4) {
//          head.color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
//          other.color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
          head.health = math.max(head.health - 0.01,0)
          other.health = math.max(other.health - 0.01,0)
        }
      })
    }





    elements foreach { elem =>
      elem._2 match {
        case e: Bowman => {
          if ((Vec(e.x, e.y) - e.target).length() < 1)
            e.move = false
          if (e.move) {
            var toTarget = e.target - Vec(e.x, e.y)
            var length = toTarget.length()
            toTarget /= length
            e.x += toTarget.x * e.speed * math.min(length / 5, 1)
            e.y += toTarget.y * e.speed * math.min(length / 5, 1)
          }
          e.notifyClientViews
          if (!zone.contains(e)) {
            //     println("Il faut sortir de " + zone.x + " " + zone.y)
            hostPool.getHyperHost(e.x, e.y).method("addBowman", e)
            elements -= elem._1
          }
        }
        case _ => {}
      }
    }
  }

  override def clientInput(id: String, data: String): Unit = {

    println("DATA RECEIVED : " + data)
    val json = Json.parse(data)
    val id = (json \ "id").get.as[String]
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]

    if(elements.contains(id)){
      val bm = elements(id).asInstanceOf[Bowman]
      bm.move = true
      bm.target = Vec(x, y)
    }



  }
}

class IdGiver(val id: String) extends Observable {}

class StgySpecialHost(hostPool: HostPool) extends AbstractSpecialHost[StgyClientView](hostPool) {

  var rand = new Random()

  override def OnConnect(id: String, obs: Observer) = {
    var color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
    val bowmen = 0 until 10 map { i => new Bowman(rand.nextInt(800), rand.nextInt(800), Random.alphanumeric.take(10).mkString, id, color) }
    bowmen foreach {
      b =>
        hostPool.getHyperHost(b.x, b.y) method("addBowman", b)
        b.sub(obs)
    }


    //tell the client view what is the client id, this is a hack

    var idGiver = new IdGiver(id)
    idGiver.sub(obs)
    idGiver.notifyClientViews

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }
}


class Stgy extends FunSuite {
  test("Stgy") {
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
    val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => actorSystem.actorOf(Props(new StgyHost(hostPool, new Zone(hostPool.fromI2X(i) * hostWidth, hostPool.fromI2Y(i) * hostHeight, hostWidth, hostHeight))), "host_" + i) }
    hostPool.addHost(hosts)
    val specialHost = actorSystem.actorOf(Props(new StgySpecialHost(hostPool)), "specialHost")

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
