package core.script_filled

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractClientView
import core.user_import.Zone
import core.user_import.Element

//Placeholder to be replaced by pre-compilation script

class UserClientView(hosts: HostPool, client: ActorRef) extends AbstractClientView(hosts,client) {
  // USER JOB
  override def dataToViewZone(): List[Zone] = List(new Zone(0,0,300,300))

  override def onNotify(any: Any): Unit = println(any)

  override def fromListToClientMsg(list: List[Any]) = {
    val string = "fromListToClientMsg: "+ list.map(  e=> e match{

      case e: Element => {
        "(" + e.x + "," + e.y + ")"
      }

      case _ => "NOTELEMENT : " + e


    }).mkString(",")
    println(string)
    string
  }
}
