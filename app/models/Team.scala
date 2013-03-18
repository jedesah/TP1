package models

import scala.slick.driver.PostgresDriver.simple._

case class Team(id: Option[Int] = None, name1: String, name2: String)

object Teams extends Table[Team]("team") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	
	def name1 = column[String]("name1")
	
	def name2 = column[String]("name2")
	
	def * = id.? ~ name1 ~ name2 <>(Team, Team.unapply _)
	
	def autoInc = name1 ~ name2 returning id
}