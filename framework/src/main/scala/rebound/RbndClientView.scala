package rebound

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractClientView
import stgy.StgyHost


class RbndClientView(hostPool: HostPool[RbndHost], client: ActorRef) extends AbstractClientView(hostPool, client) {

}
