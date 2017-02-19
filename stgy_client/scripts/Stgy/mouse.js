module.exports.onDragEnd = (f) => { toDoOnDragEnd = f }
module.exports.onDrag = (f) => { toDoWhileDrag = f }
module.exports.onDragRightEnd = (f) => { toDoOnDragRightEnd = f }
module.exports.onDragRight = (f) => { toDoWhileDragRight = f }
module.exports.onTrailEnd = (f) => { toDoOnTrailEnd = f }
module.exports.onTrail = (f) => { toDoWhileTrail = f }
module.exports.getDrag = () => { return active ? drag : null }
module.exports.getDragRight = () => { return activeRight ? dragRight : null }
module.exports.getPosition = () => getPosition()
var toDoOnDragEnd = () => { }
var toDoWhileDrag = () => { }
var toDoOnDragRightEnd = () => { }
var toDoWhileDragRight = () => { }
var toDoWhileTrail = () => { }
var toDoOnTrailEnd = () => { }
var active = false
var activeRight = false
var activeTrail = false
var position = { x: 0, y: 0 }
var drag = { p1: { x: 0, y: 0 }, p2: { x: 0, y: 0 } }
var dragRight = { p1: { x: 0, y: 0 }, p2: { x: 0, y: 0 } }
var trail = []
const getPosition = () => { return { x: parseInt(position.x), y: parseInt(position.y) } }

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
    if (activeTrail) {
        var last = trail[trail.length - 1]
        if (Math.abs(last.x - position.x) > 5 || Math.abs(last.y - position.y) > 5)
            trail.push(getPosition())
        toDoWhileTrail(trail)
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
    if (e.button == 1) {
        activeTrail = true
        trail=[]
        trail.push(getPosition())
        toDoWhileTrail(trail)
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
    if(e.button ==1){
        activeTrail=false
        toDoOnTrailEnd(trail)
    }
})