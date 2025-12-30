import dev.cheleb.sbt.fullstackjs.OnLoad

val scala3 = "3.7.4"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3

val filterConsoleScalacOptions = { options: Seq[String] =>
  options.filterNot(Set(
    "-Xfatal-warnings",
    "-Werror",
    "-Wdead-code",
    "-Wunused:imports",
    "-Ywarn-unused:imports",
    "-Ywarn-unused-import",
    "-Ywarn-dead-code"
  ))
}

// ...

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Wunused:all",
  "-Xfatal-warnings"
)

ThisBuild / dependencyOverrides += "org.scala-lang" %% "scala3-library" % scala3 // ScalaJS workaround

// ThisBuild / Test / scalacOptions ~= filterConsoleScalacOptions
// ThisBuild / console / scalacOptions ~= filterConsoleScalacOptions
// ThisBuild / Test / scalacOptions ~= filterConsoleScalacOptions
// ThisBuild / console / scalacOptions ~= filterConsoleScalacOptions

ThisBuild / run / fork := true

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val Versions = new {
  val zio        = "2.1.23"
  val tapir      = "1.10.7"
  val zioJson    = "0.7.1"
  val zioLogging = "2.2.4"
  val zioConfig  = "4.0.2"
  val sttp       = "3.9.6"
  val javaMail   = "1.6.2"
  val stripe     = "26.7.0"
  val flywaydb   = "10.17.1"
}

val commonDependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % Versions.tapir,
  "com.softwaremill.sttp.client3" %% "zio"               % Versions.sttp
)

val serverDependencies = commonDependencies ++ Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-zio"                         % Versions.tapir, // Brings in zio, zio-streams
  "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"             % Versions.tapir, // Brings in zhttp
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"           % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"            % Versions.tapir % "test",
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"                 % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"                    % Versions.tapir,
  "com.softwaremill.sttp.client3" %% "zio"                               % Versions.sttp,
  "dev.zio"                       %% "zio-logging"                       % Versions.zioLogging,
  "dev.zio"                       %% "zio-logging-slf4j"                 % Versions.zioLogging,
  "ch.qos.logback"                 % "logback-classic"                   % "1.5.23",
  "dev.zio"                       %% "zio-test"                          % Versions.zio,
  "dev.zio"                       %% "zio-test-junit"                    % Versions.zio   % "test",
  "dev.zio"                       %% "zio-test-sbt"                      % Versions.zio   % "test",
  "dev.zio"                       %% "zio-test-magnolia"                 % Versions.zio   % "test",
  "dev.zio"                       %% "zio-mock"                          % "1.0.0-RC12"   % "test",
  "dev.zio"                       %% "zio-config"                        % Versions.zioConfig,
  "dev.zio"                       %% "zio-config-magnolia"               % Versions.zioConfig,
  "dev.zio"                       %% "zio-config-typesafe"               % Versions.zioConfig,
  "io.getquill"                   %% "quill-jdbc-zio"                    % "4.8.6",
  "org.postgresql"                 % "postgresql"                        % "42.7.8",
  "org.flywaydb"                   % "flyway-core"                       % Versions.flywaydb,
  "org.flywaydb"                   % "flyway-database-postgresql"        % Versions.flywaydb,
  "io.github.scottweaver"         %% "zio-2-0-testcontainers-postgresql" % "0.10.0",
  "dev.zio"                       %% "zio-prelude"                       % "1.0.0-RC44",
  "com.auth0"                      % "java-jwt"                          % "4.5.0",
  "com.sun.mail"                   % "javax.mail"                        % Versions.javaMail,
  "com.stripe"                     % "stripe-java"                       % Versions.stripe
)

lazy val foundations = (project in file("modules/foundations"))
  .settings(
    libraryDependencies ++= serverDependencies
  )

lazy val common = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/common"))
  .settings(
    libraryDependencies ++= commonDependencies
  )
// .jsSettings(
//   libraryDependencies ++= Seq(
//     "io.github.cquiroz" %%% "scala-java-time" % "2.6.0" // implementations of java.time classes for Scala.JS,
//   )
// )

lazy val server = (project in file("modules/server"))
  .enablePlugins(FullstackJsPlugin)
  .settings(scalaJsProject := app)
  .settings(
    libraryDependencies ++= serverDependencies
  )
  .dependsOn(common.jvm)

lazy val app = (project in file("modules/app"))
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"    % Versions.tapir,
      "com.softwaremill.sttp.client3" %%% "zio"               % Versions.sttp,
      "dev.zio"                       %%% "zio-json"          % Versions.zioJson,
      "dev.zio"                       %%% "zio-prelude"       % "1.0.0-RC44",
      "com.raquo"                     %%% "laminar"           % "17.0.0",
      "io.frontroute"                 %%% "frontroute"        % "0.19.0" // Brings in Laminar 16
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    semanticdbEnabled               := true,
    autoAPIMappings                 := true,
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass             := Some("com.rockthejvm.reviewboard.App")
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(common.js)

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .settings(
    name := "zio-rite-of-passage"
  )
  .aggregate(server, app)
  .dependsOn(server, app)

// //
// // This is a global setting that will generate a build-env.sh file in the target directory.
// // This file will contain the SCALA_VERSION variable that can be used in the build process
// //
// Global / onLoad := {

//   OnLoad.apply((app / scalaVersion).value, root, app)

//   (Global / onLoad).value
// }
