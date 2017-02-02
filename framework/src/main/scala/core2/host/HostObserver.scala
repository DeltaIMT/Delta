package core2.host

import core2.clientView.ClientViewRef

abstract class HostObserver[T] extends InputReceiver {

  var id2ClientView = collection.mutable.HashMap[String, ClientViewRef[T]]()

}
