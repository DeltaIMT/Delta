package core.host

import core.clientView.{ClientView, ClientViewActor, ClientViewRef}

//a special host for the data that cannot be spatialized
abstract class HostObserver[T<: ClientView] extends InputReceiver {

  var id2ClientView = collection.mutable.HashMap[String, ClientViewRef[T]]()

}
