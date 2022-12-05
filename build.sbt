val zioVersion = "2.0.4"
val zioLogger = "2.1.5"
val zioJson = "0.3.0"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.krishna",
        version := "0.0.1",
        scalaVersion := "3.2.1",
        description := "",
      )
    ),
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

scalacOptions ++= Seq(
  "-Yretain-trees" // To use the default parameters for the ZIO JSON for Scala 3
)
