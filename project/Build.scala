import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "TP1"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe" % "slick_2.10.0-RC1" % "0.11.2",
    "com.typesafe" % "play-slick_2.10" % "0.3.0",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("github repo for play-slick", url("http://loicdescotte.github.com/releases/"))(Resolver.ivyStylePatterns)
  )

}
