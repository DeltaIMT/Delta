package core.port_dispatch

import java.time.Clock

import akka.actor.{Actor, ActorRef}
import core.CoreMessage._


class ProviderPort(numberOfClient : Int, providers : Seq[ActorRef]) extends Actor {
  var availablePorts = (9001 to 9001+numberOfClient-1).toList


  override def receive: Receive = {
    case AddClient(id: String, client: ActorRef) => {
      if (availablePorts.isEmpty){
        println ( "a pu de place")
      }else {
        client ! PlayersUpdate("" + availablePorts.head)

        providers.seq(availablePorts.head - 9001) ! FromProviderPort(self, availablePorts.head)

        availablePorts = availablePorts.tail
        println(availablePorts.toString() + " on connection")
      }
    }

    case DeleteClient(id) => {
//      println("providerPort disconnected   " + id )
      println(availablePorts.toString() + " when ws0 is out")
    }

    case ClientDisconnection(port:Int) => {
      availablePorts = port::availablePorts
//      println("Client disconnected from port :" + port)
      println(availablePorts.toString() + " on disconnection")

    }
  }
}

