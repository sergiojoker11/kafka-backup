
import sbt.Keys._
import sbt._

object Common {

  lazy val values: Seq[Setting[_]] = Seq(
    organization := "com.sj11",
    name := "kafka-backup",
    version := "0.1.0",
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:postfixOps",
      "-feature",
      "-Xfatal-warnings",
      "-unchecked",
      "-Xlint:unused"
    ),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
}