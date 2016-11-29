package core

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, ClientInputWithLocation, ConnectClient, DeleteClient}
import play.api.libs.json.{JsArray, Json}

class Provider(hosts: HostPool, specialHost: ActorRef) extends Actor{

  var clientRef : ActorRef = null

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      specialHost ! AddClient(id, playerActorRef)
    }

    case DeleteClient(id)=> {
      specialHost ! DeleteClient(id)
    }

    case x:ClientInputWithLocation => {

      val jsonObject = Json.parse(x.command).asInstanceOf[JsArray].value

      jsonObject foreach {j =>  {

        val hosts = (j \ "hosts" ).get.asInstanceOf[JsArray].value map {x => x.asInstanceOf[JsArray].value.map(y=> y.as[String].toDouble)    }


       println(hosts map {x => x.mkString(",")} mkString(";") )

      //  val data = (j \ "data" ).get.as[String]

     //   println("hosts :" + hosts.map(x => x.mkString(",")).mkString(",")   )
     //   println("data  :" + data)

      }}


    }

    case _ => {}
  }
}
