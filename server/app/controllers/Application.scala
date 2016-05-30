package controllers

import play.api.{Environment, Configuration}
import play.api.mvc._

class Application(configuration: Configuration)(implicit environment: Environment) extends Controller {
  def index(path: String) = Action {
    val flickrKey = configuration.getString("flickr-key").getOrElse(sys.error("flickr-key not found in config"))
    Ok(views.html.index("Title", flickrKey))
  }
}
