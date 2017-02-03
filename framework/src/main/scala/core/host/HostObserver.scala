package core.host

import core.clientView.{ClientView, ClientViewActor, ClientViewRef}

abstract class HostObserver[T<: ClientView] extends InputReceiver {

  var id2ClientView = collection.mutable.HashMap[String, ClientViewRef[T]]()

}
