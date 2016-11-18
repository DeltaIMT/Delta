package core

import akka.actor.ActorRef
import core.user_import.Element

class HyperHost(val host : ActorRef)  {

  def foreachRemote() = ???
  def foreach() = ???
  def set() = ???
  def getList() :List[Element] = ???
  def getListFilter(f : Element => Boolean ) :List[Element] = ???

}
