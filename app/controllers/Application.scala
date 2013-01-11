package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
  	import scala.util.Random
  	val possibleDayNames = List("Turday", "Mayday", "Voday", "Olay", "Hulay", "Sunday", "Nightday", "Moonday", "Jupiday", "Pluday")
    Ok(views.html.index(1 + Random.nextInt(10000), Random.shuffle(possibleDayNames).take(7)))
  }
  
}