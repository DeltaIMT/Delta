import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}

/**
  * Created by vannasay on 20/10/16.
  */
class WebSocket(regionMap: Map[String, ActorRef]) {
  val rand = scala.util.Random
  val playerActorSource= Source.actorRef[GameEvent](60,OverflowStrategy.fail)
  var region = regionMap("region")
  def flow(id: String, regionName: String) : Flow[Message,Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource) { implicit builder => playerActor =>
    import GraphDSL.Implicits._
    println(id + " connected")
    val materialization = builder.materializedValue.map(playerActorRef => AddPlayer(Player( id, Vector(0,0),Vector(0,0),40, Array(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)),null), playerActorRef))
    val merge = builder.add(Merge[GameEvent](2))

    val messageToEventFlow = builder.add(Flow[Message].map {
      case TextMessage.Strict(txt) => Command(id,txt)
    })
    val eventToMessageFlow= builder.add(Flow[GameEvent].map{
      case PlayersUpdate(json) =>TextMessage(json)
    })
    val RegionSink = Sink.actorRef[GameEvent](region,DelPlayer(id))

    materialization ~> merge
    messageToEventFlow ~> merge
    merge ~> RegionSink
    playerActor ~>  eventToMessageFlow
    FlowShape(messageToEventFlow.in, eventToMessageFlow.out)
  })

  def changeRegion(regionName: String) = {
    region = regionMap(regionName)
  }
}
