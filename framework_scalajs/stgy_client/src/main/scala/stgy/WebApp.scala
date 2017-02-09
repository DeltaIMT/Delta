package stgy.scalajs

import scala.scalajs.js.JSApp
import org.scalajs.dom
import dom.{FileReader, UIEvent, document, window}
import dom.raw.Blob
import org.scalajs.dom.html.Canvas
import boopickle.Default._
import core.FrontFramework

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.typedarray.ArrayBuffer
import scalajs.js.typedarray.TypedArrayBuffer
import scala.util.{Failure, Success}

case class Color(r: Int, g: Int, b: Int) {
  def apply() = s"""rgb(${r},${g},${b})"""
  def /(x : Float) = Color( (r/x).toInt ,(g/x).toInt,(b/x).toInt)
  def *(x : Float) = Color( (r*x).toInt ,(g*x).toInt,(b*x).toInt)

}

case class StgyFrame(units: List[UnityFront], arrows: List[ArrowFront], camX: Float, camY: Float, xp: Float, numberOfUnit: Int)

case class UnityFront(id: String, color: Color, x: Int, y: Int, health: Float)

case class ArrowFront(id: String, x: Int, y: Int)

object WebApp extends JSApp {
  type Context = dom.CanvasRenderingContext2D

  val loop: (Double) => Unit = (currentTime) => {
    val elapsedTime = currentTime - lastTime
    lastTime = currentTime
    val frame = Interp.interpFrame(currentTime)
  if(frame != null)
    Drawer.draw(context, frame)

    FrontFramework.getPing(println)
    window.requestAnimationFrame(loop)
  }


  var lastTime = 0.0
  var context: Context = _

  override def main(): Unit = {
    context = appendCanvasAndGetContext()
    val future = FrontFramework.launch
    future.onComplete {
      case Success(b) => {
        startDataManipulation()
        window.requestAnimationFrame(loop)
      }
      case Failure(e) => println("FAILURE : " + e)
    }
  }






  def startDataManipulation() = {

    FrontFramework.dataManipulation {
      case b: Blob => {
        blob2ArrayBuffer(b).onComplete {
          case Success(ab) => {
            println("Received : " + ab.byteLength + " bytes")
            val bytebyffer = TypedArrayBuffer.wrap(ab)

            val frame = Unpickle[StgyFrame].fromBytes(bytebyffer)

            Interp.addFrame(frame)

          }
          case Failure(e) => {}
        }

      }
    }

  }

  def blob2ArrayBuffer(blob: Blob): Future[ArrayBuffer] = {
    val result = Promise[ArrayBuffer]()
    val fr = new FileReader

    fr.onload = { (ui: UIEvent) => result.success(fr.result.asInstanceOf[ArrayBuffer]) }
    fr.readAsArrayBuffer(blob)
    result.future
  }


  def appendCanvasAndGetContext(): Context = {
    val canvas = document.createElement("canvas").asInstanceOf[Canvas]
    document.body.appendChild(canvas)
    document.body.style.margin = "0px"
    document.body.style.padding = "0px"
    canvas.width = window.innerWidth.toInt
    canvas.height =window.innerHeight.toInt
    canvas.getContext("2d").asInstanceOf[Context]
  }
}
