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
	val maxGrade = 20
	val grade = if (success) 20 else 0
	addGradeToDB("Part 2", matricules, grade)
	Ok(views.html.feedback(grade, maxGrade, Nil))
      }
      case _ => BadRequest("You did something wrong")
    }
  }

  def uploadGit = Action(parse.multipartFormData) { request =>
    val matricules = request.body.asFormUrlEncoded("matricule").head
    (request.body.file("file"), matricules) match {
      case (Some(file), matricules) => {
	val (grade, reasons) = verifyTodoApplication(new java.util.zip.ZipFile(file.ref.file))
	addGradeToDB("Part 2", matricules, grade)
	Ok(views.html.feedback(grade, 80, reasons))
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

  private def addGradeToDB(partName: String, matricules: String, grade: Int) {
    database withSession {
      val grading = AssignementGrading(None, partName, grade)
      val id = (AssignementGradings returning AssignementGradings.id insert grading)
      for (matricule <- matricules.split(' ')) {
	val userGrading = UserGrading(None, matricule.toInt, id)
	(UserGradings insert userGrading)
      }
    }
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

  private def verifyTodoApplication(zipFile: java.util.zip.ZipFile): (Int, List[String]) = {
    import collection.JavaConverters._
    val entries = zipFile.entries.asScala.toList
    val names = entries.map(_.getName)
    val gitReason = if (names.exists(_.endsWith("/.git/")))
      None else Some("This directory does not appear to be versioned with Git")
    val applicationReason = if (names.exists(_.endsWith("app/controllers/Application.scala")))
      None else Some("Expected a file named Application.scala in directory named controllers")
    val taskReason = if (names.exists(_.endsWith("app/models/Task.scala")))
      None else Some("Expected a file named Task.scala in directory named models")
    val viewsReason = if (names.exists(_.endsWith("app/views/")))
      None else Some("Expected to find a directory named views")
    val reasons = List(gitReason, applicationReason, taskReason, viewsReason).filter(_.isInstanceOf[Some[_]])
    (80 - (reasons.length * 20), reasons.map(_.get))
  }
}