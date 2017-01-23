package core.port_dispatch

import java.time.Clock

import akka.actor.{Actor, ActorRef}
import core.CoreMessage._


class ProviderPort(numberOfClient: Int, providers: Seq[ActorRef]) extends Actor {
  var availablePorts = (9001 to 9001 + numberOfClient - 1).toList

  override def receive: Receive = {
    case AddClient(id: String, client: ActorRef) => {
      if (availablePorts.isEmpty) {
        println("No free Provider")
      } else {
        client ! PlayersUpdate("" + availablePorts.head)
        providers.seq(availablePorts.head - 9001) ! FromProviderPort(self, availablePorts.head)
        availablePorts = availablePorts.tail
      }
    }

    case DeleteClient(id) => {
    }

    case ClientDisconnection(port: Int) => {
      availablePorts = port :: availablePorts
    }
  }
}

