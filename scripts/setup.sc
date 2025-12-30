#!/usr/bin/env -S scala-cli -S 3

//> using scala "3.8.0-RC4"
// using javaOptions "--sun-misc-unsafe-memory-access=allow" // Example option to set maximum heap size
//> using dep "com.lihaoyi::os-lib:0.11.6"
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit
import java.time.Instant

import os.*
import scala.math.Ordered.orderingToOrdered

val buildSbt     = os.pwd / "build.sbt"
val buildEnv     = os.pwd / "scripts" / "target" / "build-env.sh"
val devMarker    = os.pwd / "target" / "dev-server-running.marker"
val npmDevMarker = os.pwd / "target" / "npm-dev-server-running.marker"

val client          = os.pwd / "modules" / "app"
val nodeModule      = client / "node_modules" / ".package-lock.json"
val packageJson     = client / "package.json"
val packageLockJson = client / "package-lock.json"

os.remove(devMarker)
os.remove(npmDevMarker)

if buildSbt isYoungerThan buildEnv then
  println(s"Importing project settings into build-env.sh ($buildEnv)...")
  os.proc("sbt", "projects")
    .call(
      cwd = os.pwd,
      env = Map("INIT" -> "setup"),
      stdout = os.ProcessOutput.Readlines(line => println(s"  $line"))
    )

npmCommand foreach: command =>
  println(s"✨ Installing ($command) node modules...")
  os.proc("npm", command).call(cwd = client)
  println("Node modules installation complete.")

def npmCommand: Option[String] =
  if packageLockJson.isMissing then
    println("✨\t- First install")
    Some("install")
  else if packageJson isYoungerThan packageLockJson then
    println("⏫\t- package.json has been modified since the last installation.")
    Some("install")
  else if nodeModule.isOlderThanAWeek then {
    print(s"\t- 🔎 Node modules already installed but old")
    println("\n\t\t- ⚠️\t Not installed recently ( > 7 days). Consider reinstalling if issues arise.")
    None
  } else if nodeModule.isMissing then {
    println("🟢 CI")
    Some("ci")
  } else {
    println("✅ uptodate.")
    None
  }

extension (path: Path)
  /** True if something must be reprocessed.
    */
  infix def isYoungerThan(that: Path) =
    if os.exists(that) then
      os.stat(path).mtime > os.stat(that).mtime
    else true

  def exists: Boolean = os.exists(path)

  def isMissing: Boolean = !os.exists(path)

  def isOlderThanAWeek: Boolean =
    os.exists(path) && os.stat(path).mtime.toInstant < Instant.now().minus(7, ChronoUnit.DAYS)
