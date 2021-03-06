package models

import scala.slick.driver.PostgresDriver.simple._

case class AssignementGrading(id: Option[Int] = None, name: String, grade: Int)

object AssignementGradings extends Table[AssignementGrading]("assignementGrading") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  
  def name = column[String]("name")
  
  def grade = column[Int]("grade")
  
  def * = id.? ~ name ~ grade <>(AssignementGrading, AssignementGrading.unapply _)

  def autoInc = name ~ grade returning id
}