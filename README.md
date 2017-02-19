# Delta framework

![](delta_logo.png)

#### What's inside
The repository is comprised of 2 kind of directories. 
- /framework/ contains the framework itself 
- /xxxx_server/  and /xxxx_client/ contains server and client side instanced framework application named xxxx

Both a server and a client need to run. The framework itself can't be launched.
It needs to be instanciated. Several examples are present :
- Stgy    : a strategy game
- Chps    : a cooperative pirate ship game
- Paint   : a multiplayer paint
- Splatoon: a shooter game inspired by Splatoon  

#### How to run the framework 

##### Requirement
- [Node] v6.9.5 LTS
- [Scala] v2.11.8
- [SBT] v0.13

##### Procedure

With Intellij :
Open the \demo_server\src\main\scala\demo directory
Launch Demo.scala

Then with node in the \demo_client\ directory 
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
  