import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.NativePackagerKeys
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.linux.LinuxKeys
import sbt.Keys.{mappings, name, resourceDirectory}
import sbt._

object DockerSettings extends NativePackagerKeys with LinuxKeys {
  val dockerSettings: Seq[Def.Setting[_]] = Seq(
    dockerBaseImage := "<<some base image>>",
    dockerVersion := Some(DockerVersion(18, 9, 0, Some("ce"))),
    dockerRepository := Some("sergiojoker11/kafka-backup"),
    Docker / packageName := name.value,
    dockerUpdateLatest := true,
    Docker / defaultLinuxInstallLocation := "/app",
    dockerExposedPorts += 8072,
    Docker / mappings ++= {
      val resDir = (Compile / resourceDirectory).value
      val jmxExporterConf = resDir / "jmx_exporter.yml"
      val jmxExporterConfTargetPath = "/app/jmx_prometheus_javaagent/jmx_exporter.yml"

      Seq(
        jmxExporterConf -> jmxExporterConfTargetPath
      )
    },
    dockerCommands := {
      dockerCommands.value.flatMap {
        case ExecCmd("CMD") =>
          Seq(
            ExecCmd(
              "CMD",
              "-Dlogback.configurationFile=/app/config/logback.xml"
            )
          )
        case other => Seq(other)
      }
    }
  )
}