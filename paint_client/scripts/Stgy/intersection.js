module.exports.pointInsideSquare = (point, square) => {
    var x =point.x
    var y =point.y
    var minX = Math.min(square.p1.x, square.p2.x)
    var maxX = Math.max(square.p1.x, square.p2.x)
    var minY = Math.min(square.p1.y, square.p2.y)
    var maxY = Math.max(square.p1.y, square.p2.y)
    return (x < maxX && x > minX && y < maxY && y > minY)
}