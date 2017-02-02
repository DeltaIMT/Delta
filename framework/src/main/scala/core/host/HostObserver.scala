package core.host

import core.clientView.{ClientViewActor, ClientViewRef}

abstract class HostObserver[T<: ClientViewActor] extends InputReceiver {

  var id2ClientView = collection.mutable.HashMap[String, ClientViewRef[T]]()

}
