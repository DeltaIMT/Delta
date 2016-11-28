//
//import java.io.{OutputStream, PrintStream}
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
//import akka.stream.ActorMaterializer
//import core.`abstract`.{AbstractClientView, AbstractHost, AbstractSpecialHost, UpdateClient}
//import core.{FakeClient, HostPool, Provider, Websocket}
//import core.user_import.{Element, Zone}
//import org.scalatest.FunSuite
//
//
//class AbstractClientViewTest extends FunSuite {
//
//
//  //defining some user classes needed to compile
//  class UserClientView(hosts: HostPool, client: ActorRef) extends AbstractClientView(hosts, client) {
//    override def dataToViewZone(): List[Zone] = List(new Zone(0, 0, 300, 300))
//
//    override def onNotify(any: Any): Unit = println(any)
//
//    override def fromListToClientMsg(list: List[Any]) = {
//      val string = list.map(e => e match {
//        case e: Element => {
//          "(" + e.x + "," + e.y + ")"
//        }
//        case _ => "NOTELEMENT : " + e
//      }).mkString(",")
//      //println(string)
//      string
//    }
//  }
//
//  class UserHost(hostPool: HostPool) extends AbstractHost(hostPool) {
//
//    elements += "1" -> new Element(1, 2)
//
//  }
//
//  class UserSpecialHost extends AbstractSpecialHost(null) {
//
//  }
//
//
//  test("AbstractClientViewTest") {
//
//    println("framework starting")
//    implicit val actorSystem = ActorSystem("akka-system")
//    implicit val flowMaterializer = ActorMaterializer()
//    val initialPort = 5000
//    val numberOfClient = 100
//    val hostsGridWidth = 5
//    val hostsGridHeight = 5
//    val hostWidth = 200
//    val hostHeight = 200
//
//    val hostPool = new HostPool(hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
//    val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => actorSystem.actorOf(Props(new UserHost(hostPool)), "host" + i) }
//    hostPool.addHost(hosts)
//    val specialHost = actorSystem.actorOf(Props(new UserSpecialHost()), "specialHost")
//
//    val providers = 0 until numberOfClient map { i => actorSystem.actorOf(Props(new Provider(hostPool, specialHost)), "provider_" + i) }
//    val websockets = 0 until numberOfClient map { i => initialPort + i -> new Websocket(providers(i), initialPort + i) }
//
//    println("framework working")
//
//    val routes = websockets.map(x => {
//      x._1 ->
//        (get & parameter("id")) {
//          id => handleWebSocketMessages(x._2.flow(id, "region"))
//        }
//    })
//
//    val interface = "localhost"
//
//    routes foreach { route =>
//      Http().bindAndHandle(route._2, "0.0.0.0", route._1)
//    }
//
//    //TEST CLIENT VIEW
//    val fakeClient = actorSystem.actorOf(Props(new FakeClient()), "fakeclient")
//    val clientViewTest = actorSystem.actorOf(Props(new UserClientView(hostPool, fakeClient)), "clientview")
//
//
//    for (i <- 0 to 100) {
//
//      Thread.sleep(1)
//
//      clientViewTest ! UpdateClient
//    }
//    //END TEST CLIENT VIEW
//
//
//    println("framework shutting down")
//    actorSystem.terminate()
//    println("framework down")
//  }
//
//
//}
