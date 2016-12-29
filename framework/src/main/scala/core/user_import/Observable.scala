package core.user_import

import akka.actor.ActorRef
import core.`abstract`.Notify

trait Observable {


  def notifyClientViews: Unit ={
    clientViews.foreach(cv => {
      cv ! Notify(this)
    }
    )
  }
  def sub(userClientView : ActorRef): Unit = {
    clientViews = userClientView :: clientViews
  }

  def sub(obs : Observer): Unit = {
    clientViews = obs.client :: clientViews
  }

  def unSub(u: ActorRef) : Unit = {
    clientViews = clientViews.filter( p => p!=u)
  }
  def unSub(obs : Observer) : Unit = {
    clientViews = clientViews.filter( p => p!=obs.client)
  }

  var clientViews = List[ActorRef]()

}
