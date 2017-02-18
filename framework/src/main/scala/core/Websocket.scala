package core

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, Materializer, OverflowStrategy}
import akka.util.ByteString
import core.CoreMessage._

import scala.concurrent.ExecutionContext.Implicits.global

class Websocket(val provider : ActorRef, val port : Int, implicit val mat : Materializer) {
  val playerActorSource= Source.actorRef[Any](10000,OverflowStrategy.fail)

  def flow(id: String) : Flow[Message,Message, Any] = Flow.fromGraph(GraphDSL.create(playerActorSource) { implicit builder => playerActor =>
    import GraphDSL.Implicits._
  //  println("Websocket flow called " + id + " on port : " + port)
    val materialization = builder.materializedValue.map(playerActorRef => AddClient(id, playerActorRef))
    val merge = builder.add(Merge[Any](2))

    val messageToEventFlow = builder.add(Flow[Any].map {
      case TextMessage.Strict(txt) => ClientInputWithLocation(id,txt)
      case x:TextMessage.Streamed => {
        val sink = Sink.fold[String,String]("")(_+_)
        val truc = x.getStreamedText.runWith(Sink.fold("")(_+_),mat)
          truc.onComplete( t =>   provider !  ClientInputWithLocation(id,t.get ) )
      }
    })
    val eventToMessageFlow= builder.add(Flow[Any].map{
      case PlayersUpdate(json) =>TextMessage(json)
      case PlayersUpdateRaw(bytes) =>  akka.http.scaladsl.model.ws.BinaryMessage(ByteString.fromByteBuffer(bytes))
    })
    val ProviderSink = Sink.actorRef[Any](provider,DeleteClient(id))

    materialization ~> merge
    messageToEventFlow ~> merge
    merge ~> ProviderSink
    playerActor ~>  eventToMessageFlow
    FlowShape(messageToEventFlow.in, eventToMessageFlow.out)
  })
}
/*


      Client                    Command
        +                          +
        |                          |
        |                          |
        v                          |
+-------+-------+       +----------v---------+
|               |       |                    |
|materialization|       | messageToEventFlow |
|               |       |                    |
+-------+-------+       +----------+---------+
        |                          |
        |                          |
        +--------+   +-------------+
                 |   |
                 |   |
                 |   |
                 |   |
                 |   |
                 |   |
               +-v---v--+
               |        |
               | merge  |
               |        |                     Client
               +---+----+                       +
                   |                            |
                   |                            |
                   |                 +----------v---------+
           +-------v-------+         |                    |
           |               |         | eventToMessageFlow |
           |  ProviderSink |         |                    |
           |               |         +--------------------+
           +---------------+




 */

