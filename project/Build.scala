import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "TP1"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe" % "slick_2.10.0-RC1" % "0.11.2",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
