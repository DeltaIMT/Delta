class Snake {
    constructor(id, l, x, y) {
        this.id = id
        this.l = l
        this.positions = [{x: x, y: y}]
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