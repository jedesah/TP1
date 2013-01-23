package controllers

import play.api._
import play.api.mvc._

import scala.slick.driver.PostgresDriver.simple._

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
  
  def uploadV8 = Action(parse.multipartFormData) { request =>
    parseUploadAndGrade(request, verifyV8Patch, 20, "TP1 Part 2")
  }

  def uploadGit = Action(parse.multipartFormData) { request =>
    parseUploadAndGrade(request, verifyTodoApplication, 80, "TP1 Part 1")
  }

  def parseUploadAndGrade(request: Request[MultipartFormData[play.api.libs.Files.TemporaryFile]], correction: (java.io.File) => (Int, List[String]), maxGrade: Int, name: String) = {
    val accentProblemExplanation = "Notice Temporaire: Pour une raison qu'on ignore, les accents ne semblent pas tres bien être supporté par la version de Java sur le serveur. Svp, enlever le é dans le mot revision du fichier diff produit par les postes Fedora à l'école. Si ça ne règle pas votre problème, contactez le charge de lab"
    try {
      val matricule1Str = request.body.asFormUrlEncoded("matricule1").head
      val matricule1 = validateMatricule(matricule1Str)
      val matricule2Str = request.body.asFormUrlEncoded("matricule2").head
      val matricules = matricule1 :: (if (matricule2Str != "") List(validateMatricule(matricule2Str)) else Nil)
      val file = request.body.file("file").get
      val (grade, reasons) = correction(file.ref.file)
      addGradeToDB(name, matricules, grade)
      Ok(views.html.feedback(grade, maxGrade, reasons))
    } catch {
      case InvalidMatriculeError(msg) => BadRequest(msg)
      case e: NoSuchElementException => BadRequest("No file was submitted")
      case e: java.util.zip.ZipException => BadRequest("Your file is in an unsupported zip format")
      case e: java.io.IOException => BadRequest("An unknow io exception has occured. Make sure your submission file(s) are valid. " + accentProblemExplanation)
      case _: Throwable => BadRequest("Le systeme semble avoir rencontre une erreur. " + accentProblemExplanation)
    }
  }

  def validateMatricule(matriculeString: String): Int = {
    if (matriculeString.length != 7)
      throw InvalidMatriculeError(s"""Matricule: "$matriculeString" must be 7 digits long""")
    try {
      matriculeString.toInt
    }
    catch {
      case e: NumberFormatException =>
	throw InvalidMatriculeError(s"""Matricule: "$matriculeString" must contain only digits""")
    }
  }

  case class InvalidMatriculeError(msg: String) extends Exception

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

  private def addGradeToDB(partName: String, matricules: List[Int], grade: Int) {
    database withSession {
      val id = AssignementGradings.autoInc.insert(partName, grade)
      for (matricule <- matricules) {
	UserGradings.autoInc.insert(matricule, id)
      }
    }
  }

  private def verifyV8Patch(file: java.io.File): (Int, List[String]) = {
    val source = io.Source.fromFile(file)
    val listOfLines = source.getLines.toList
    if (!listOfLines(0).endsWith("src/date.js")) (0, List("Expected different file to be modified"))
    else {
      val removedLine = listOfLines.exists(_ == "-var LongWeekDays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday',")
      val addedLine = listOfLines.exists(_.startsWith("+var LongWeekDays = ['"))
      if (removedLine && addedLine) (20, Nil)
      else (0, List("Expected different modification to file \"src/date.js\""))
    }
  }

  private def verifyTodoApplication(file: java.io.File): (Int, List[String]) = {
    val zipFile = new java.util.zip.ZipFile(file)
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