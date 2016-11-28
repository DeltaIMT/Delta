package core.script_filled

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractSpecialHost

//Placeholder to be replaced by pre-compilation script

class UserSpecialHost(hostPool: HostPool) extends AbstractSpecialHost(hostPool){

  //USER
  override def OnConnect(clientId: ActorRef): Unit = {}
}
