package paint

import core.host.HostObserver

class PaintHostObserver extends HostObserver[PaintClientView] {
  override def clientInput(id: String, data: String): Unit = {

  }

  def associateToClientView(id: String, x:Double, y:Double): Unit = {
    id2ClientView(id).call(cv => {cv.x = x
                                  cv.y = y})
  }
}
