package stgy.scalajs

import org.scalajs.dom
import dom.document
import stgy.Vec

object Mouse {

  def onDrag( func : ((Vec,Vec))=>Unit  ) = {toDoWhileDrag = func}
  def onDragEnd( func : ((Vec,Vec))=>Unit  ) = {toDoOnDragEnd = func}
  def onDragRight( func : ((Vec,Vec))=>Unit  ) = {toDoWhileDragRight = func}
  def onDragRightEnd( func : ((Vec,Vec))=>Unit  ) = {toDoOnDragRightEnd = func}
  def onTrail( func : (List[Vec])=>Unit  ) = {toDoWhileTrail = func}
  def onTrailEnd( func : (List[Vec])=>Unit  ) = {toDoOnTrailEnd = func}

  private var active = false
  private var activeRight = false
  private var activeTrail = false
  private var position = Vec(0, 0)
  private var drag = (Vec(0, 0), Vec(0, 0))
  private var dragRight = (Vec(0, 0), Vec(0, 0))
  private var trail = List[Vec]()
  private var toDoOnDragEnd = (mouse: (Vec, Vec)) => {}
  private var toDoWhileDrag = (mouse: (Vec, Vec)) => {}
  private var toDoOnDragRightEnd = (mouse: (Vec, Vec)) => {}
  private var toDoWhileDragRight = (mouse: (Vec, Vec)) => {}
  private var toDoWhileTrail = (trail: List[Vec]) => {}
  private var toDoOnTrailEnd = (trail: List[Vec]) => {}

  def getPosition: Vec = Vec(position.x,position.y)


  document.addEventListener("mousemove", (mouseMoveEvent: dom.MouseEvent) => {
    position.x = mouseMoveEvent.pageX
    position.y = mouseMoveEvent.pageY
    if (active) {
      drag = (drag._1, getPosition)
      toDoWhileDrag(drag)
    }
    if (activeRight) {
      dragRight = (dragRight._2, getPosition)
      toDoWhileDragRight(dragRight)
    }
    if (activeTrail) {
      var last = trail.last
      if ((getPosition - last).length() > 5)
        trail ::= getPosition
      toDoWhileTrail(trail)
    }
  })

  document.addEventListener("mousedown", (mouseMoveEvent: dom.MouseEvent) => {
    if (mouseMoveEvent.button == 0) {
      active = true
      drag = (getPosition, getPosition)
      toDoWhileDrag(drag)
    }
    if (mouseMoveEvent.button == 2) {
      activeRight = true
      dragRight = (getPosition, getPosition)
      toDoWhileDragRight(dragRight)
    }
    if (mouseMoveEvent.button == 1) {
      activeTrail = true
      trail = getPosition :: Nil
      toDoWhileTrail(trail)
    }
  }
  )

  document.addEventListener("mouseup", (mouseMoveEvent: dom.MouseEvent) => {
    if (mouseMoveEvent.button == 0) {
      active = false
      drag = (drag._1, getPosition)
      toDoOnDragEnd(drag)
    }
    if (mouseMoveEvent.button == 2) {
      activeRight = false
      dragRight = (dragRight._1, getPosition)
      toDoOnDragRightEnd(dragRight)
    }
    if (mouseMoveEvent.button == 1) {
      activeTrail = false
      toDoOnTrailEnd(trail)
    }
  }
  )
}
