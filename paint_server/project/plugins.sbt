logLevel := Level.Warn

//addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.6")
//// Bring the sbt-aspectj settings into this build
//aspectjSettings
//
//// Here we are effectively adding the `-javaagent` JVM startup
//// option with the location of the AspectJ Weaver provided by
//// the sbt-aspectj plugin.
//javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj
//
//// We need to ensure that the JVM is forked for the
//// AspectJ Weaver to kick in properly and do it's magic.
//fork in run := true

//Add the aspectj-runner plugin to project/plugins.sbt
addSbtPlugin("io.kamon" % "aspectj-runner" % "0.1.4")

// Run!
// aspectj-runner:run