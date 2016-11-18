var provided = require('./providedCode')



/* 

-------------------------------------------------------------------------
TODO write your code here 
-------------------------------------------------------------------------

*/



/* declare here the actions you would like to perform when the client 
receives information from the server */
var jsonDataManipulation = () => {
  //TODO
}

/* declare here what the client should send back to the server : 
replace arg1, arg2, value1, value2 and add some if you want to */
var defineCommandToServer = () => {
  var command = { "arg1" : value1, "arg2" : value2} //TODO
  return command
}

/* this function sends json data (defined by the defineCommandToServer()
function) to the server */
sendCommandToServer()