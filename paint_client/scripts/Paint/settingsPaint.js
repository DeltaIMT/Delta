triggerColorSettings = () => {
    var exports = module.exports

    var cr = prompt("Change the default color (RGB values, red value) : ", 255)
    var cg = prompt("Change the default color (RGB values, green value) : ", 0)
    var cb = prompt("Change the default color (RGB values, blue value) : ", 179)

    exports.customColor = [parseInt(cr),parseInt(cg),parseInt(cb)]
}

triggerThicknessSettings = () => {
    var exports = module.exports

    var t = prompt("Change the default thickness : ", 12)

    exports.customThickness = parseInt(t)
}

triggerEraseMode = () => {
    var exports = module.exports

    var erase = prompt("Erase mode (0 = OFF, 1 = ON) : ", 0)

    exports.erasing = parseInt(erase)
}