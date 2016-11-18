package core.user_import

trait Host {
  def tick()
  def onMsg(msg : String)
}
