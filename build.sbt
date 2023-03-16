val zioVersion = "2.0.10"
val zioLogger  = "2.1.11"
val zioJson    = "0.4.2"

ThisBuild / organization := "com.krishna"
ThisBuild / version      := "0.0.1"
ThisBuild / description  := "ZIO based Scala project"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val zioRedisExclusionRules = Seq(
  ExclusionRule("dev.zio"),
  ExclusionRule("org.scala-lang.modules")
)

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
      "dev.zio"  %% "zio-http"  % "0.0.3",

      // https://github.com/profunktor/redis4cats
      "dev.profunktor" %% "redis4cats-effects" % "1.4.0",

      // https://zio.github.io/zio-logging/
      "ch.qos.logback" % "logback-classic"   % "1.4.5",
      "dev.zio"       %% "zio-logging"       % zioLogger,
      "dev.zio"       %% "zio-logging-slf4j" % zioLogger
    ) ++
      zioConfigDependencies ++
      flywayMigrationDependencies ++
      doobieDependencies ++
      authScala
  )

// https://zio.dev/zio-config/
val zioConfig = "3.0.7"

val zioConfigDependencies: Seq[ModuleID] = Seq(
  "dev.zio" %% "zio-config"          % zioConfig,
  "dev.zio" %% "zio-config-typesafe" % zioConfig,
  "dev.zio" %% "zio-config-magnolia" % zioConfig
)

// https://flywaydb.org/
val flywayMigrationDependencies: Seq[ModuleID] = Seq(
  "org.flywaydb"   % "flyway-core" % "9.15.2",
  "org.postgresql" % "postgresql"  % "42.5.4"
)

// https://tpolecat.github.io/doobie/
val doobieJdbc = "1.0.0-RC2"

val doobieDependencies: Seq[ModuleID] = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieJdbc,
  "org.tpolecat" %% "doobie-postgres"  % doobieJdbc,
  "org.tpolecat" %% "doobie-hikari"    % doobieJdbc,
  "dev.zio"      %% "zio-interop-cats" % "23.0.0.0"
)

// https://jwt-scala.github.io/jwt-scala/jwt-core-jwt.html
val authScala: Seq[ModuleID] = Seq(
  "com.github.jwt-scala" %% "jwt-core"     % "9.2.0",
  ("com.github.t3hnar"   %% "scala-bcrypt" % "4.3.0").cross(CrossVersion.for3Use2_13)
)

Global / onChangedBuildSource := ReloadOnSourceChanges

// ============= SBT Aliases ============================
addCommandAlias("api", "~reStart;")
addCommandAlias("status", "reStatus;")

addCommandAlias("update", "dependencyUpdates;")

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

   // @formatter:off
  s"""|
      |${header(" _______ ___       ___  _   _  ___ _____ ___ ")}
      |${header("|_  /_ _/ _ \\ ___ / _ \\| | | |/ _ \\_   _| __|")}
      |${header(" / / | | (_) |___| (_) | |_| | (_) || | | _| ")}
      |${header("/___|___\\___/     \\__\\_\\\\___/ \\___/ |_| |___|")}
      |
      |Useful sbt tasks:
      |${item("api")}: Run the Scala project that will restart automatically if there is a new change.
      |${item("status")}: See the sbt status of the project.
      |${item("fmt")}: Prepares source files using scalafix and scalafmt.
      |${item("sFix")}: Fixes sources files using scalafix.
      |${item("fmtCheck")}: Checks sources by applying both scalafix and scalafmt.
      |${item("sFixCheck")}: Checks sources by applying both scalafix.
      |
      |${subItem("GitHub account: https://github.com/krishna-thapa/zio-inspirational-quote")}
      """.stripMargin
  // @formatter:on
}
