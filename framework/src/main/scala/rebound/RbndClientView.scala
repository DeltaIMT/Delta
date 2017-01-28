package rebound

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractClientView
import core.user_import.Zone
import stgy.{Aggregator, IdGiver, StgyHost, Unity}


class RbndClientView(hostPool: HostPool[RbndHost], client: ActorRef) extends AbstractClientView(hostPool, client) {

  var ball: Ball = _

  override def dataToViewZone(): List[Zone] = List(new Zone(ball.x - 1500, ball.y - 1500, 3000, 3000))

  override def onNotify(any: Any): Unit = {

    any match {
      case b: Ball => {
        ball = b
      }
      case _ => {
        println("notify not matched")
      }
    }
  }


  override def fromListToClientMsg(list: List[Any]) = {

    val listString = list.map {
      case e: Ball => {
        s"""{"type":"ball","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}","energy":"${e.energy}"}"""
      }
      case e: Wall => {
        s"""{"type":"wall","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}","x2":"${e.x2.toInt}","y2":"${e.y2.toInt}"}"""
      }
    } ++ List(
      s"""{"type":"camera","id":"0","x":"${ball.x.toInt}","y":"${ball.y.toInt}"}""",
      s"""{"type":"other","id":"1","energy":"${ball.energy}"}""")
    val string = listString.mkString("[", ",", "]")
    string

  }
}
