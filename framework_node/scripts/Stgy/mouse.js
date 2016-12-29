module.exports.onDragEnd = (f) => { toDoOnDragEnd = f }
module.exports.onDrag = (f) => { toDoWhileDrag = f }
module.exports.onDragRightEnd = (f) => { toDoOnDragRightEnd = f }
module.exports.onDragRight = (f) => { toDoWhileDragRight = f }
module.exports.getDrag = () => { return active ? drag : null }
module.exports.getDragRight = () => { return activeRight? dragRight : null }
module.exports.getPosition = () => getPosition()
var toDoOnDragEnd = () => { }
var toDoWhileDrag = () => { }
var toDoOnDragRightEnd = () => { }
var toDoWhileDragRight = () => { }
var active = false
var activeRight = false
var position = { x: 0, y: 0 }
var drag = { p1: { x: 0, y: 0 }, p2: { x: 0, y: 0 } }
var dragRight = { p1: { x: 0, y: 0 }, p2: { x: 0, y: 0 } }
const getPosition = () => { return { x: position.x, y: position.y } }

document.addEventListener('mousemove', function (mouseMoveEvent) {
    position.x = mouseMoveEvent.pageX
    position.y = mouseMoveEvent.pageY
    if (active) {
        drag.p2 = getPosition()
        toDoWhileDrag(drag)
    }
    if (activeRight) {
        dragRight.p2 = getPosition()
        toDoWhileDragRight(dragRight)
    }
}, false)

document.addEventListener('mousedown', function (e) {
    if (e.button == 0) {
        active = true
        drag.p1 = getPosition()
        drag.p2 = getPosition()
        toDoWhileDrag(drag)
    }
    if (e.button == 2) {
        activeRight = true
        dragRight.p1 = getPosition()
        dragRight.p2 = getPosition()
        toDoWhileDragRight(dragRight)
    }
})

document.addEventListener('mouseup', function (e) {
    if (e.button == 0) {
        active = false
        drag.p2 = getPosition()
        toDoOnDragEnd(drag)
    }
    if (e.button == 2) {
        activeRight = false
        dragRight.p2 = getPosition()
        toDoOnDragRightEnd(dragRight)
    }
})