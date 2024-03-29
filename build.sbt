val circeVersion = "0.14.5"
val logStashVersion = "7.3"
val typesafeConfigVersion = "1.4.2"
val fs2KafkaVersion = "3.0.1"
val vulcanVersion = "1.8.3"
val taggingVersion = "2.3.4"
val fs2IoVersion = "3.9.2"
val log4CatsVersion = "2.5.0"
val kafkaVersion = "3.4.0"

lazy val commonDeps: Seq[ModuleID] = Seq(
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion
)

lazy val productionDeps: Seq[ModuleID] = Seq(
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.softwaremill.common" %% "tagging" % taggingVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "net.logstash.logback" % "logstash-logback-encoder" % logStashVersion,
  "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,
  "co.fs2" %% "fs2-io" % fs2IoVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion
)

lazy val testDeps: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,
  "io.github.embeddedkafka" %% "embedded-kafka" % kafkaVersion % Test
)

lazy val overrideDeps: Seq[ModuleID] = Seq(
)

lazy val deps: Seq[Def.Setting[Seq[ModuleID]]] = Seq(
  dependencyOverrides ++= overrideDeps,
  libraryDependencies ++= commonDeps ++ productionDeps ++ testDeps
)

lazy val commonTestJavaOptions = Seq("-Dconfig.file=src/main/resources/dev-local.conf")

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent, JavaAppPackaging)
  .settings(deps)
  .settings(Common.values)
  .settings(
    javaAgents += JavaAgent(
      "io.prometheus.jmx" % "jmx_prometheus_javaagent" % "0.18.0" % "compile",
      arguments = "5556:/app/jmx_prometheus_javaagent/jmx_exporter.yml")
  )
  .settings(DockerSettings.dockerSettings)
  .settings(
    Defaults.itSettings,
    Test / parallelExecution := false,
    IntegrationTest / fork := true,
    IntegrationTest / javaOptions ++=
      Seq(
        s"-Dconfig.file=${Option(System.getProperty("config.file")).getOrElse("src/it/resources/it.conf")}",
        "-Dlogback.configurationFile=/src/it/resources/logback-test.xml"
      )
  )
  .configs(
    IntegrationTest extend Test
  )

lazy val backupMainClassName = "com.sj11.kafka.backup.BackupMain"
lazy val backupDebuggingPort = sys.env.get("DEBUGGING_PORT").getOrElse(5025)
lazy val runBackup = taskKey[Unit]("Run backup service with development configuration")
runBackup / javaOptions ++= commonTestJavaOptions
runBackup / fork := true
runBackup / javaOptions += s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$backupDebuggingPort"
fullRunTask(runBackup, Compile, backupMainClassName)

lazy val restoreMainClassName = "com.sj11.kafka.backup.RestoreMain"
lazy val restoreDebuggingPort = sys.env.get("DEBUGGING_PORT").getOrElse(5026)
lazy val runRestore = taskKey[Unit]("Run restore service with development configuration")
runRestore / javaOptions ++= commonTestJavaOptions
runRestore / fork := true
runRestore / javaOptions += s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$restoreDebuggingPort"
fullRunTask(runRestore, Compile, restoreMainClassName)

addCommandAlias("dockerFileTask", "docker:stage")

ThisBuild / useCoursier := false
