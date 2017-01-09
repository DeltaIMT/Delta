package core.`abstract`

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import core.CoreMessage.{AddClient, DeleteClient}
import core.HostPool
import core.user_import.Observer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import ru._


class AbstractSpecialHost[T <:AbstractClientView : TypeTag : ClassTag](val hostPool: HostPool) extends Actor{
  import ru._
  var clients = collection.mutable.HashMap[String,(Observer,Cancellable)]()
  var idClient = 0

  def OnConnect(id:String ,obs : Observer) : Unit = {}
  def OnDisconnect(id:String ,obs : Observer) : Unit = {}

  def createInstance[T:TypeTag](ar : ActorRef) : Any= {
    createInstance(typeOf[T],ar)
  }

  def createInstance(tpe:Type,ar : ActorRef): Any = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym = tpe.typeSymbol.asClass
    val clsMirror = mirror.reflectClass(clsSym)
    val ctorSym = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorMirror = clsMirror.reflectConstructor(ctorSym)
    val instance = ctorMirror(hostPool,ar)
    return instance
  }


  override def receive: Receive = {
    case AddClient(id, clientActorRef) => {
      val clientView = context.actorOf(Props(createInstance[T](clientActorRef).asInstanceOf[T]  ))
      val cancellable = context.system.scheduler.schedule(1000 milliseconds, 33.33 milliseconds,clientView,UpdateClient)
      clients += (id -> (new Observer(id,clientView),cancellable))
      OnConnect(id, clients(id)._1)
    //  clientActorRef ! PlayersUpdate("you are connected")
    }


    case DeleteClient(id) => {
      clients(id)._2.cancel()
      OnDisconnect(id ,clients(id)._1)
      clients-= id
    }

    case _ => {}
  }
}
