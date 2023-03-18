import sbt.Keys.*
import sbt.{ Def, * }
import scalafix.sbt.ScalafixPlugin.autoImport.*

object BuildHelper:
  val ScalaVersion = "3.2.2"

  val commonSettings: Seq[String] = Seq(
    "-Yretain-trees" // To use the default parameters for the ZIO JSON for Scala 3
  )

  def stdSettings
    : Seq[Def.Setting[? >: Boolean with String with Task[Seq[String]] with Seq[sbt.ModuleID]]] =
    Seq(
      ThisBuild / fork              := true,
      ThisBuild / scalaVersion      := ScalaVersion,
      ThisBuild / scalacOptions     := commonSettings,
      ThisBuild / semanticdbEnabled := true,
      ThisBuild / semanticdbVersion := scalafixSemanticdb.revision,
      ThisBuild / scalafixDependencies ++=
        List(
          "com.github.liancheng" %% "organize-imports" % "0.6.0"
        )
    )
