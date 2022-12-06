val zioVersion = "2.0.4"
val zioLogger = "2.1.5"
val zioJson = "0.3.0"

// give the user a nice default project!
ThisBuild / organization := "com.krishna"
ThisBuild / version := "0.0.1"
ThisBuild / description := ""

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
      "dev.zio" %% "zio-logging" % zioLogger,
    ) ++ zioConfigDependencies,
  )

// https://zio.dev/zio-config/
val zioConfig = "3.0.2"
val zioConfigDependencies: Seq[ModuleID] = Seq(
  "dev.zio" %% "zio-config" % zioConfig,
  "dev.zio" %% "zio-config-typesafe" % zioConfig,
  "dev.zio" %% "zio-config-magnolia" % zioConfig,
)

addCommandAlias("fmt", "scalafmt; Test / scalafmt; sFix;")
addCommandAlias("fmtCheck", "scalafmtCheck; Test / scalafmtCheck; sFixCheck")
addCommandAlias("sFix", "scalafix OrganizeImports; Test / scalafix OrganizeImports")
addCommandAlias("sFixCheck", "scalafix --check OrganizeImports; Test / scalafix --check OrganizeImports")
