package models

import scala.slick.driver.PostgresDriver.simple._

case class UserGrading(id: Option[Int] = None, userId: Int, gradingId: Int)

object UserGradings extends Table[UserGrading]("userGrading") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  
  def userId = column[Int]("userId")
  
  def gradingId = column[Int]("gradingId")
  
  def * = id.? ~ userId ~ gradingId <>(UserGrading, UserGrading.unapply _)

  def autoInc = userId ~ gradingId returning id
}