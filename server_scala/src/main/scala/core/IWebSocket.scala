package core

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow

trait IWebSocket{

  def flow(id: String, regionName: String) : Flow[Message,Message, Any]

}