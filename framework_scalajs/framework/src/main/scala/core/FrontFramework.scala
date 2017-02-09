package core

import org.scalajs.dom
import org.scalajs.dom.{MessageEvent, WebSocket, window}
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.Date
import scala.util.Random


object FrontFramework {
  type WS = dom.WebSocket
  private var dataManipulationFunction = (a: Any) => {}
  private var now1: Date = new Date()
  private var pingCallback: (Int) => Unit = _
  private var ws: WS = _

  def launch: Future[Boolean] = {
    val p = Promise[Boolean]()

    var id = Random.alphanumeric.take(10).mkString
    var host = window.location.hostname
    if (host.length == 0) host = "127.0.0.1"
    val wsPort = new WebSocket("ws://" + host + ":9000" + "/?id=" + id)
    println("Searching port at 9000")

    wsPort.onmessage = (event: MessageEvent) => {
      val data = event.data
      println("Connection to : " + "ws://" + host + ":" + data + "/?id=" + id)
      ws = new WebSocket("ws://" + host + ":" + data + "/?id=" + id)
      ws.onmessage = (event: MessageEvent) => {
        if (!p.future.isCompleted) p.success(true)
        var data = event.data
        if (data == "ping") {
          var now2 = new Date()
          val ping = now2.getMilliseconds() - now1.getMilliseconds()
          pingCallback(ping)
        }
        else {
          dataManipulationFunction(data)
        }
      }
      wsPort.close(1000, "job done")
    }


    p.future
  }

  def getPing(callback: (Int) => Unit) = {
    ws.send("ping")
    now1 = new Date()
    pingCallback = callback
  }

  def dataManipulation(f: Any => Unit) = {
    dataManipulationFunction = f
  }

  def send(s: String) = {
    ws.send(s)
  }
}
