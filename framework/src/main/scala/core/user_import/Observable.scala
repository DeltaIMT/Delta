package core.user_import

import akka.actor.ActorRef
import core.`abstract`.Notify

class Observable {


  def notifyClientViews: Unit ={
    clientViews.foreach(cv => {
      cv ! Notify
    }
    )
  }
  def sub(userClientView : ActorRef): Unit = {
    clientViews = userClientView :: clientViews
  }
  def unSub(u: ActorRef) : Unit = {
    clientViews = clientViews.filter( p => p!=u)
  }
  var clientViews = List[ActorRef]()

}
