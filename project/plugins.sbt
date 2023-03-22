// https://scalameta.org/scalafmt/
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// https://scalacenter.github.io/scalafix/
// https://github.com/liancheng/scalafix-organize-imports
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")

// https://github.com/spray/sbt-revolver
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// https://www.baeldung.com/scala/sbt-dependency-tree
// sbt dependencyBrowseGraph
// sbt dependencyBrowseTree
addDependencyTreePlugin

// https://github.com/Philippus/sbt-dotenv
addSbtPlugin("nl.gn0s1s" % "sbt-dotenv" % "3.0.0")

// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// https://github.com/sbt/sbt-native-packager
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")