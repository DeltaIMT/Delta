package core.user_import

import akka.actor.ActorRef




class Element(var x:Double,var y:Double) {

  def update(h : ActorRef , s : String){
    h ! Set(s, this)
  }

}
