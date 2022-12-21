logLevel := Level.Info

classpathTypes += "maven-plugin"

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")
