package controllers

import play.api._
import play.api.mvc._

import scala.slick.driver.H2Driver.simple._

import scala.slick.session.Session
import play.api.db._
import play.api.Play.current

import Database.threadLocalSession

import models.{AssignementGrading, AssignementGradings}
import models.{UserGrading, UserGradings}

object Application extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())
  
  def index = Action {
    import scala.util.Random
    val possibleDayNames = List("Turday", "Mayday", "Voday", "Olay", "Hulay", "Sunday", "Nightday", "Moonday", "Jupiday", "Pluday")
    Ok(views.html.index(1 + Random.nextInt(10000), Random.shuffle(possibleDayNames).take(7)))
  }
  
  def upload = Action(parse.multipartFormData) { request =>
    val matricules = request.body.asFormUrlEncoded("matricule").head
    (request.body.file("file"), matricules) match {
      case (Some(file), matricules) => {
	val success = verifyV8Patch(io.Source.fromFile(file.ref.file))
	if (success) {
	  database withSession {
	    val grading = AssignementGrading(None, "Part 2", 20)
	    println(AssignementGradings.getClass)
	    val id = (AssignementGradings returning AssignementGradings.id insert grading)
	    println(id)
	    for (matricule <- matricules.split(' ')) {
	      val userGrading = UserGrading(None, matricule.toInt, id)
	      (UserGradings insert userGrading)
	    }
	  }
	  Ok("Tu as recus 20/20 pour la partie 2 du TP1")
	}
	else Ok("Tu as recus 0/20 pour la partie 2 du TP1. Tu peux televerser une nouvelle solution pour modifier ton score pour cette partie du TP.")
      }
      case _ => BadRequest("You did something wrong")
    }
  }

  def showGrades = Action {
    val grades = database withSession {
      val query = for {
	userGrading <- UserGradings
	grading <- AssignementGradings if grading.id === userGrading.gradingId
      } yield ConstColumn(" ") ++ userGrading.userId.asColumnOf[String] ++ " " ++ grading.name ++ " " ++ grading.grade.asColumnOf[String]
      query.list
    }
    Ok(grades.mkString("\n"))
  }

  private def verifyV8Patch(source: io.Source) = {
    val listOfLines = source.getLines.toList
    if (!listOfLines(0).endsWith("src/date.js")) false
    else if (listOfLines(4).startsWith("@@ -205")) true
    else if (listOfLines(4).startsWith("@@ -223")) true
    else if (listOfLines(4).startsWith("@@ -204")) true
    else if (listOfLines(4).startsWith("@@ -222")) true
    else if (listOfLines(4).startsWith("@@ -206")) true
    else if (listOfLines(4).startsWith("@@ -224")) true
    else false
  }
}