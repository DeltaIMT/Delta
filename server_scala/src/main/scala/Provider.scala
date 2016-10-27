import akka.actor.Actor
import akka.actor.Actor.Receive


object Provider {

  def apply(default : Actor) = {
  new Provider(default)
  }

}

class Provider(default : Actor) extends Actor {

  var map_ID_Actor = Map.empty[Int,Actor]
  var map_ID_Client = Map.empty[Int,Actor]

  override def receive: Receive = {

    case AddClient(id: String, client: Actor) => {
      map_ID_Actor[id] = default
      default ! AddClient(id,client)
      map_ID_Client[id] = client
    }

    case DeleteClient(id : String) => {
      default ! DeleteClient(id)
      map_ID_Actor[id] = null
      map_ID_Client[id] = null
    }

    case Command(id:  String, txt : String) =>{
      map_ID_Actor[id] ! Command(id,txt)
    }

    case ChangeActor(id: String, Actor nextActor) =>{
      nextActor ! AddClient(id,map_ID_Client[id] )
      map_ID_Actor[id] = nextActor
    }
  }


}
