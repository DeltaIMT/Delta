# Delta framework

#### What's inside
The framework is comprised of 2 directories. 
- /framework/ contains a akka server 
- /framework_node/ contains a node server

Both need to be ran. The framework itself can't be launched.
It needs to be instanciated. Several examples are present :
- Stgy : a strategy game
- Chps : a cooperative pirate ship game

#### How to run the framework 


##### Requirement
- [Node] v6.9.5 LTS
- [Scala] v2.11.8
- [SBT] v0.13

##### Procedure

###### In the directory /framework

With Intellij :
Open the /framework directory
Launch an instance, for example 
framework/main/scala/stgy/Stgy.scala

Without : 
http://www.scala-sbt.org/0.13/docs/index.html

###### In the directory /framework_node

In a command line
```sh
$ npm install
$ npm start
```
Open [localhost:5000]

   [SBT]:<http://www.scala-sbt.org/download.html>
   [Node]:<https://nodejs.org/en/>
   [Scala]:<https://www.scala-lang.org/download/2.11.8.html>
   [localhost:5000]: <http://localhost:5000>
  

