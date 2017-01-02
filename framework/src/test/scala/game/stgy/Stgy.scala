package game.stgy

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.Tick
import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost}
import core.port_dispatch.ProviderPort
import core.user_import.{Observable, Observer, Zone}
import core.{HostPool, Provider, Websocket}
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Random


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
      case e: Arrow => {
        s"""{"type":"arrow","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}","color":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
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

  methods += "addUnity" -> ((arg: Any) => {
    var e = arg.asInstanceOf[Unity]
    elements += e.id -> e
  })

  override def tick(): Unit = {

    val bowmen = elements.filter(e => e._2.isInstanceOf[Bowman]).values.asInstanceOf[Iterable[Bowman]]
    val arrows = elements.filter(e => e._2.isInstanceOf[Arrow]).values.asInstanceOf[Iterable[Arrow]]

    arrows foreach {
      a => {
        if (a.shouldDie) {
          elements -= a.id
        }

        else
          a.doMove

        val enemy = bowmen filter { b => b.clientId != a.clientId }

        enemy.foreach(e => {
          if ((Vec(e.x, e.y) - Vec(a.x, a.y)).length < 20) {
            e.damage(0.201)
            elements -= a.id
          }

        })

      }
    }

    bowmen foreach { A => {
      A.step
      if (A.isDead)
        elements -= A.id

      var closest: (Double, Bowman) = (Double.MaxValue, null)
      bowmen.foreach(B => {

        if (B.clientId != A.clientId) {
          val distance = (Vec(A.x, A.y) - Vec(B.x, B.y)).length()
          if (distance < closest._1)
            closest = (distance, B)
        }
      })

      if (A.canShoot && closest._1 < 350) {
        val arrow = A.shoot(Vec(closest._2.x, closest._2.y))
        elements += arrow.id -> arrow
      }


      A.notifyClientViews
    }
    }

    elements foreach { elem => {
      val e = elem._2
      if (!zone.contains(e)) {
        //     println("Il faut sortir de " + zone.x + " " + zone.y)
        hostPool.getHyperHost(e.x, e.y).method("addUnity", e)
        elements -= elem._1
      }
    }
    }


  }

  override def clientInput(id: String, data: String): Unit = {

    //  println("DATA RECEIVED : " + data)
    val json = Json.parse(data)
    val id = (json \ "id").get.as[String]
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]

    if (elements.contains(id)) {
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
        hostPool.getHyperHost(b.x, b.y) method("addUnity", b)
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


object Stgy extends App {


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


  import scala.swing._

  class UI extends MainFrame {
    title = "GUI for Delta Server"
    def shutdown = {
      println("framework shutdown")
      cancellable foreach { c => c.cancel() }
      actorSystem.terminate()
      println("Done")
    }
    contents = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Look at me!")
      contents += Swing.VStrut(10)
      contents += Swing.Glue
      contents += Button("Shutdown") {shutdown}
      contents += Button("Close") { shutdown;sys.exit(0) }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }

  val ui = new UI
  ui.visible = true

  //    Thread.sleep(1000000)

}
