import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}

/**
  * Created by vannasay on 20/10/16.
  */
class WebSocket(provider: ActorRef) {
  val rand = scala.util.Random
  val playerActorSource= Source.actorRef[GameEvent](60,OverflowStrategy.fail)
  def flow(id: String, regionName: String) : Flow[Message,Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource) { implicit builder => playerActor =>
    import GraphDSL.Implicits._
    println(id + " connected")
    val materialization = builder.materializedValue.map(playerActorRef => AddClient(id, playerActorRef))
    val merge = builder.add(Merge[Any](2))

    val messageToEventFlow = builder.add(Flow[Any].map {
      case TextMessage.Strict(txt) => Command(id,txt)
    })
    val eventToMessageFlow= builder.add(Flow[Any].map{
      case PlayersUpdate(json) =>TextMessage(json)
    })
    val ProviderSink = Sink.actorRef[Any](provider,DeleteClient(id))

    materialization ~> merge
    messageToEventFlow ~> merge
    merge ~> ProviderSink
    playerActor ~>  eventToMessageFlow
    FlowShape(messageToEventFlow.in, eventToMessageFlow.out)
  })
}