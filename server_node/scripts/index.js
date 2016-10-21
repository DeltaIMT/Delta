import * as d3 from "d3";
var canvas = d3.select("div")
.append("svg")
.attr("width",500)
.attr("height",500)

var grayBackground = canvas
.append("rect")
.attr("x", 0)
.attr("y", 0)
.attr("width", 500)
.attr("height", 500)
.attr("fill",d3.rgb(200, 210, 220))

var jsonData = []
var id = require('./App')
var host = window.location.hostname
var frames = 0
var ws = new WebSocket('ws://' + host + ':8080'+"/?id="+id );
      ws.onmessage = function (event) {
        frames++
      // console.log( event.data)
        jsonData = JSON.parse(event.data)      
      };

var w = window.innerWidth;
var h = window.innerHeight;

var mousePosition = {x:250, y:250};
document.addEventListener('mousemove', function(mouseMoveEvent){

  if (mouseMoveEvent.pageX > 500 || mouseMoveEvent.pageX<0
   || mouseMoveEvent.pageY<0 || mouseMoveEvent.pageY >500)
{
  mousePosition.x = 250
  mousePosition.y = 250
}
else
{
  mousePosition.x = mouseMoveEvent.pageX;
  mousePosition.y = mouseMoveEvent.pageY;
}

}, false);

var sendCommand = () =>
{
setTimeout(sendCommand,16.667)
//console.log(mousePosition)
var command = { "mouse" : mousePosition }
ws.send( JSON.stringify(command)  )
}
setTimeout(sendCommand,1000)


var countFPS = () =>
{
  setTimeout(countFPS,1000)
  console.log("fps : " +frames)
  frames = 0
  console.log(JSON.stringify(jsonData,1));
}
setTimeout(countFPS,1000)


var draw = () =>
{
   canvas.selectAll("circle")
        .data ([]).exit().remove()

        canvas.selectAll("circle")
        .data (jsonData)
        .enter()
        .append("circle")
        .attr("cx", (e) => e.pos.x)
        .attr("cy", (e) => e.pos.y)
        .attr("r", (e)=> e.r)
        .attr("fill", (e) => d3.rgb(e.color[0] , e.color[1],e.color[2])  )


window.requestAnimationFrame(draw);
} 

window.requestAnimationFrame(draw);