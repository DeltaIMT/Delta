package core.user_import

import akka.actor.ActorRef
import core.HyperHost




class Element(var x:Double,var y:Double) {

  def update(h : HyperHost, s : String){
    h.host ! Set(s, this)
  }

  override def toString: String = "("+x+","+y+")"

}
