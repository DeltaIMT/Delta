package core.host


trait InputReceiver {

  //defines how to process the inputs sent by the client
  def clientInput(id :String ,data: String)
}
