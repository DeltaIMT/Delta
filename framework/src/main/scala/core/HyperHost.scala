package core

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import core.CoreMessage.{GetList, GetListFilter}
import core.user_import.Element

import scala.collection.mutable
import scala.concurrent.Future

class HyperHost(val host : ActorRef)  {
  implicit val timeout = Timeout(1.second)

  def set(id: String, element: Element) = {
    host ! Set(id,element)
  }

  def getList() : mutable.HashMap[String,Element] = {
    var list = mutable.HashMap[String,Element]()
    val futureList = host ? GetList
    futureList.map(_ => {
      _ match {
        case x: mutable.HashMap[String,Element] => {
          list = x
      }
      }
    })
    list
  }

  def getListFilter(f : Element => Boolean ) : mutable.HashMap[String,Element] = {
    var list = mutable.HashMap[String,Element]()
    val futureList = host ? GetListFilter(f)
    futureList.map(_ => {
      _ match {
        case x: mutable.HashMap[String,Element] => {
          list = x
        }
      }
    })
    list
  }

}
