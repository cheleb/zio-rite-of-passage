import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.scripts.AshScriptPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.SbtNativePackager.autoImport.*

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import org.scalajs.sbtplugin.*

import sbt.*
import sbt.Keys.*

import scalajsbundler.sbtplugin.*
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.*

//import webscalajs.WebScalaJS.autoImport.*
import scala.util.control.NoStackTrace

class NoVersionException extends RuntimeException("VERSION env variable not set") with NoStackTrace

object DeploymentSettings {
//
// Define the build mode:
// - FullStack: production mode, aka with BFF and webjar deployment
// - Docker:    production mode, aka with BFF and webjar deployment
// - dev:  development mode
//         no optimization, ESModule
//         static files, hot reload with vite.
//
// Default is "dev" mode, because the vite build does not take parameters.
//   (see vite.config.js)

  val overrideDockerRegistry = sys.env.get("LOCAL_DOCKER_REGISTRY").isDefined

  def nexusNpmSettings =
    sys.env
      .get("NEXUS")
      .map(url =>
        npmExtraArgs ++= Seq(
          s"--registry=$url/repository/npm-public/"
        )
      )
      .toSeq

  lazy val dockerSettings = {
    import DockerPlugin.autoImport.*
    import DockerPlugin.globalSettings.*
    import sbt.Keys.*
    Seq(
      Docker / maintainer     := "Joh doe",
      Docker / dockerUsername := Some("cheleb"),
      Docker / packageName    := "zio-rite-of-passage",
      dockerBaseImage         := "azul/zulu-openjdk-alpine:23-latest",
      dockerUpdateLatest      := true,
      dockerExposedPorts      := Seq(8000)
    ) ++ (overrideDockerRegistry match {
      case true =>
        Seq(
          Docker / dockerRepository := Some("registry.orb.local"),
          Docker / dockerUsername   := Some("cheleb")
        ) :+ sys.env
          .get("VERSION")
          .map(v => ThisBuild / version := v)
          .getOrElse(throw new NoVersionException)
      case false =>
        Seq()
    })
  }

}
