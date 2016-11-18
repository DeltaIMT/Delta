class Snake {
    constructor(id, x, y, r, l, rgb) {
        this.id = id
        this.positions = [{x: x, y: y}]
        this.r = r
        this.l = l
        this.rgb = rgb
    }

    add(x,y) {
        if (this.positions.length == this.l) {
            this.positions.pop();
        }
        this.positions.unshift({x: x, y: y});
    }

    is(id) {
        return this.id == id;
    }
}
module.exports = Snake