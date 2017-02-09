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

object WebApp extends JSApp {

  case class Color(r: Int, g: Int, b : Int) {
    def apply() = s"""rgb(${r},${g},${b})"""
  }
  case class StgyFrame(units: List[UnityFront],arrows: List[ArrowFront], camX: Float, camY: Float, xp: Float, numberOfUnit: Int)
  case class UnityFront(id : String, color : Color, x: Int, y : Int, health: Float)
  case class ArrowFront(id : String, x: Int, y : Int)
  type Context = dom.CanvasRenderingContext2D



  val loop: (Double) => Unit = (currentTime) => {
    val elapsedTime = currentTime - lastTime
    lastTime = currentTime

   // FrontFramework.getPing(println)
    window.requestAnimationFrame(loop)
  }


  var lastTime = 0.0

  override def main(): Unit = {
    val context = appendCanvasAndGetContext()
    Drawer.drawCircle(context)

    val future = FrontFramework.launch

    future.onComplete {
      case Success(b) => {

        FrontFramework.dataManipulation {
          case b :Blob=> {


            blob2ArrayBuffer(b) .onComplete {
             case Success(ab) => {
               println("Received : "+ab.byteLength+ " bytes")
               val bytebyffer =  TypedArrayBuffer.wrap(ab)

               val frame = Unpickle[StgyFrame].fromBytes(bytebyffer)
              // println(frame)
             }
             case Failure(e)=> {}
            }

          }
        }
        window.requestAnimationFrame(loop)
      }
      case Failure(e) => println("FAILURE : " + e)
    }


  }

  def blob2ArrayBuffer(blob: Blob): Future[ArrayBuffer] = {
    val result = Promise[ArrayBuffer]()
    val fr = new FileReader

    fr.onload = { (ui:UIEvent) => result.success(fr.result.asInstanceOf[ArrayBuffer]) }
    fr.readAsArrayBuffer(blob)
    result.future
  }


  def appendCanvasAndGetContext(): Context = {
    val canvas = document.createElement("canvas").asInstanceOf[Canvas]
    document.body.appendChild(canvas)
    canvas.width = 150
    canvas.height = 150
    canvas.getContext("2d").asInstanceOf[Context]
  }
}
