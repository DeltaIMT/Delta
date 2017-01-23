package stgy

import core.{AbstractMain, Host}
import core.CoreMessage.CallTrace
import kamon.Kamon
import scala.concurrent.duration._
import scala.swing._
import scala.concurrent.ExecutionContext.Implicits.global

object Stgy extends App {

  val main = new AbstractMain[StgyHost, StgyProvider]()
  main.numberOfClient = 100
  main.launch
  val cancellable = main.hostPool.hyperHostsMap.values.map(hh => main.actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, hh.host, CallTrace((x: Host) => x.tick(), "tick")))
  val ui = new UI

  def shutdown = {
    println("framework shutdown")
    cancellable foreach { c => c.cancel() }
    main.actorSystem.terminate()
    Kamon.shutdown()
    println("Done")
  }

  class UI extends MainFrame {
    title = "GUI for Delta Server"
    contents = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Server")
      contents += Swing.VStrut(10)
      contents += Swing.Glue
      contents += Button("Shutdown") {
        shutdown
      }
      contents += Button("Flush") {
        main.hostPool.hyperHostsMap.values foreach (_.call(_.flush()))
      }
      contents += Button("Close") {
        shutdown
        sys.exit(0)
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }
  ui.visible = true
}
