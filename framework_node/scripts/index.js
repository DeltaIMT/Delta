var client = require('./providedCode')
client.launch()

client.commandToServer( ()=>{return "truc"} )

var test = 0
client.dataManipulation( data => (test = data) )
setTimeout(() => console.log("test = " + test) , 5000)