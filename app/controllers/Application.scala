package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
  	import scala.util.Random
    Ok(views.html.index(1 + Random.nextInt(10000), 1 + Random.nextInt(100), 1 + Random.nextInt(100), 1 + Random.nextInt(100)))
  }
  
}