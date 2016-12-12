package core

import akka.actor.{Actor, ActorRef}
import core.CoreMessage._
import play.api.libs.json.{JsArray, Json}

class Provider(hosts: HostPool, specialHost: ActorRef) extends Actor{

  var clientRef : ActorRef = null

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      specialHost ! AddClient(id, playerActorRef)
      println("Provider Connection    "+id)
    }

    case DeleteClient(id)=> {
      specialHost ! DeleteClient(id)
      println("Provider Disconnection "+id)
    }

    case x:ClientInputWithLocation => {

     // println(x.command)
      val jsonObject = Json.parse(x.command).asInstanceOf[JsArray].value
   //   println(x.command)
      jsonObject foreach {j =>  {
        val hosts1 = (j \ "hosts" ).get.as[JsArray].value
        val hosts2 = hosts1 map {x => x.as[JsArray].value}
        val hosts =  hosts2 map {x => x map {y=> y.as[Double]}}
      // println(hosts map {x => x.mkString(",")} mkString(";") )

        val data = (j \ "data" ).get.as[String]
     //   println("hosts :" + hosts.map(x => x.mkString(",")).mkString(",")   )
     //   println("data  :" + data)

        hosts foreach { h =>  this.hosts.getHyperHost(h(0),h(1)).host ! ClientInput(x.id,data)   }
      }}

    }

    case _ => {}
  }
}
