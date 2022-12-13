val zioVersion = "2.0.4"
val zioLogger  = "2.1.5"
val zioJson    = "0.3.0"

ThisBuild / organization := "com.krishna"
ThisBuild / version      := "0.0.1"
ThisBuild / description  := ""

lazy val root = (project in file("."))
  .settings(BuildHelper.stdSettings)
  .settings(
    name := "zio-inspirational-quote",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,

      // https://zio.dev/reference/stream/
      "dev.zio" %% "zio-streams" % zioVersion,

      // https://zio.dev/zio-json/
      "dev.zio" %% "zio-json" % zioJson,

      // https://zio.github.io/zio-http/
      "io.d11" %% "zhttp" % "2.0.0-RC11",

      // https://zio.github.io/zio-logging/
      "ch.qos.logback" % "logback-classic"   % "1.4.5",
      "dev.zio"       %% "zio-logging"       % zioLogger,
      "dev.zio"       %% "zio-logging-slf4j" % zioLogger
    ) ++ zioConfigDependencies ++ doobieDbDependencies
  )

// https://zio.dev/zio-config/
val zioConfig = "3.0.2"

val zioConfigDependencies: Seq[ModuleID] = Seq(
  "dev.zio" %% "zio-config"          % zioConfig,
  "dev.zio" %% "zio-config-typesafe" % zioConfig,
  "dev.zio" %% "zio-config-magnolia" % zioConfig
)

// https://zio.dev/zio-quill/
val doobieDbDependencies: Seq[ModuleID] = Seq(
  "org.flywaydb"   % "flyway-core" % "9.10.0",
  "org.postgresql" % "postgresql"  % "42.5.1"
)

Global / onChangedBuildSource := ReloadOnSourceChanges

// ============= SBT Aliases ============================
addCommandAlias("api", "~reStart;")
addCommandAlias("status", "reStatus;")

addCommandAlias("fmt", "scalafmt; Test / scalafmt; sFix;")
addCommandAlias("fmtCheck", "scalafmtCheck; Test / scalafmtCheck; sFixCheck")
addCommandAlias("sFix", "scalafix OrganizeImports; Test / scalafix OrganizeImports")

addCommandAlias(
  "sFixCheck",
  "scalafix --check OrganizeImports; Test / scalafix --check OrganizeImports"
)

onLoadMessage := {
  import scala.Console.*

  def header(text: String): String  = s"${RED}$text${RESET}"
  def item(text: String): String    = s"${GREEN}> ${CYAN}$text${RESET}"
  def subItem(text: String): String = s"  ${YELLOW}> ${CYAN}$text${RESET}"

  s"""|
      |${header(" _______ ___       ___  _   _  ___ _____ ___ ")}
      |${header("|_  /_ _/ _ \\ ___ / _ \\| | | |/ _ \\_   _| __|")}
      |${header(" / / | | (_) |___| (_) | |_| | (_) || | | _| ")}
      |${header("/___|___\\___/     \\__\\_\\\\___/ \\___/ |_| |___|")}
      |
      |Useful sbt tasks:
      |${item(
       "api"
     )}: Run the Scala project that will restart automatically if there is a new change.
      |${item("status")}: See the sbt status of the project.
      |${item("fmt")}: Prepares source files using scalafix and scalafmt.
      |${item("sFix")}: Fixes sources files using scalafix.
      |${item("fmtCheck")}: Checks sources by applying both scalafix and scalafmt.
      |${item("sFixCheck")}: Checks sources by applying both scalafix.
      |
      |${subItem("GitHub account: https://github.com/krishna-thapa/zio-inspirational-quote")}
      """.stripMargin
}
