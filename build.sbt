val zioVersion = "2.0.4"
val zioLogger = "2.1.5"

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
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-logging" % zioLogger,
    ),
  )
