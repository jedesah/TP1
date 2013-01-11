package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    import scala.util.Random
    val possibleDayNames = List("Turday", "Mayday", "Voday", "Olay", "Hulay", "Sunday", "Nightday", "Moonday", "Jupiday", "Pluday")
    Ok(views.html.index(1 + Random.nextInt(10000), Random.shuffle(possibleDayNames).take(7)))
  }
  
  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      Ok(if (verifyV8Patch(io.Source.fromFile(picture.ref.file))) "Looks good" else "Was not expecting this")
    }.getOrElse {
      Ok("Failed to upload file")
    }
  }

  private def verifyV8Patch(source: io.Source) = {
    val listOfLines = source.getLines.toList
    if (!listOfLines(0).endsWith("src/date.js")) false
    else if (!listOfLines(4).startsWith("@@ -205,7")) false
    else true
  }
}