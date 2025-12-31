val scala3                     = "3.7.4"
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

inThisBuild(Seq(
  version      := "0.1.0-SNAPSHOT",
  scalaVersion := scala3,
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Wunused:all",
    "-Xfatal-warnings"
  ),
  dependencyOverrides += "org.scala-lang" %% "scala3-library" % scala3, // ScalaJS workaround
  Test / scalacOptions ~= filterConsoleScalacOptions,
  console / scalacOptions ~= filterConsoleScalacOptions,
  run / fork := true,
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
))

val Versions = new {
  val flywaydb       = "10.17.1"
  val frontroute     = "0.19.0"
  val javaJWT        = "4.5.0"
  val javaMail       = "1.6.2"
  val laminar        = "17.2.1"
  val logbackClassic = "1.5.23"
  val postgresql     = "42.7.8"
  val stripe         = "26.7.0"
  val sttp           = "3.9.6"
  val tapir          = "1.13.4"
  val testcontainers = "2.0.3"
  val zio            = "2.1.24"
  val zioConfig      = "4.0.2"
  val zioJson        = "0.7.44"
  val zioLogging     = "2.2.4"
  val zioMock        = "1.0.0-RC12"
  val zioPrelude     = "1.0.0-RC44"
  val zioQuill       = "4.8.6"
}

val commonDependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % Versions.tapir,
  "com.softwaremill.sttp.client3" %% "zio"               % Versions.sttp
)

val serverDependencies = commonDependencies ++ Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-zio"                  % Versions.tapir, // Brings in zio, zio-streams
  "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"      % Versions.tapir, // Brings in zhttp
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"    % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"     % Versions.tapir   % Test,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"          % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"             % Versions.tapir,
  "com.softwaremill.sttp.client3" %% "zio"                        % Versions.sttp,
  "dev.zio"                       %% "zio-logging"                % Versions.zioLogging,
  "dev.zio"                       %% "zio-logging-slf4j"          % Versions.zioLogging,
  "ch.qos.logback"                 % "logback-classic"            % Versions.logbackClassic,
  "dev.zio"                       %% "zio-test"                   % Versions.zio,
  "dev.zio"                       %% "zio-test-junit"             % Versions.zio     % Test,
  "dev.zio"                       %% "zio-test-sbt"               % Versions.zio     % Test,
  "dev.zio"                       %% "zio-test-magnolia"          % Versions.zio     % Test,
  "dev.zio"                       %% "zio-mock"                   % Versions.zioMock % Test,
  "dev.zio"                       %% "zio-config"                 % Versions.zioConfig,
  "dev.zio"                       %% "zio-config-magnolia"        % Versions.zioConfig,
  "dev.zio"                       %% "zio-config-typesafe"        % Versions.zioConfig,
  "io.getquill"                   %% "quill-jdbc-zio"             % Versions.zioQuill,
  "org.postgresql"                 % "postgresql"                 % Versions.postgresql,
  "org.flywaydb"                   % "flyway-core"                % Versions.flywaydb,
  "org.flywaydb"                   % "flyway-database-postgresql" % Versions.flywaydb,
  "org.testcontainers"             % "testcontainers-postgresql"  % Versions.testcontainers,
  "dev.zio"                       %% "zio-prelude"                % Versions.zioPrelude,
  "com.auth0"                      % "java-jwt"                   % Versions.javaJWT,
  "com.sun.mail"                   % "javax.mail"                 % Versions.javaMail,
  "com.stripe"                     % "stripe-java"                % Versions.stripe
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
      "dev.zio"                       %%% "zio-prelude"       % Versions.zioPrelude,
      "com.raquo"                     %%% "laminar"           % Versions.laminar,
      "io.frontroute"                 %%% "frontroute"        % Versions.frontroute
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
