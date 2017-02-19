package demo

import core.host.{HostObserver, HostPool}

class DemoHostObserver extends HostObserver[DemoClientView] {
  override def clientInput(id: String, data: String): Unit = {}
}
