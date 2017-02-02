package core2.observerPattern

import core.`abstract`.Notify

trait Observable {

  def notifyClientViews: Unit ={
    clientViews.foreach(cv => {
      cv.client ! Notify(this)
    }
    )
  }

  def sub(obs : Observer): Unit = {
    clientViews = obs :: clientViews
  }

  def unSub(obs : Observer) : Unit = {
    clientViews = clientViews.filter( p => p!=obs.client)
  }

  var clientViews: List[Observer] = List[Observer]()
}
