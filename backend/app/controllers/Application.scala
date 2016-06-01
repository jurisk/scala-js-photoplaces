package controllers

import cats.data.Xor
import net.photoplaces.protocol.{LoadPhotosRequest, LoadPhotosResponse}
import net.photoplaces.protocol.Photo
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import utils.CirceSupport

class Application(wsClient: WSClient, configuration: Configuration)(implicit environment: Environment)
  extends Controller with CirceSupport {

  val flickrKey = configuration.getString("flickr-key").getOrElse(sys.error("flickr-key not found in config"))

  case class SearchPhotosResponse(photos: SearchPhotosResponseDetails)
  case class SearchPhotosResponseDetails(photo: List[Photo])

  def index(path: String) = Action {
    Ok(views.html.index("Title"))
  }

  def loadPhotos = Action.async(circe.json[LoadPhotosRequest]) { req ⇒
    val b = req.body
    val params = Map(
      "method" -> "flickr.photos.search",
      "api_key" -> flickrKey,
      "lat" -> b.latitude.toString,
      "lon" -> b.longitude.toString,
      "format" -> "json",
      "nojsoncallback" -> "1",
      "extras" -> "geo"
    )

    // TODO: paginate?
    val url = s"https://api.flickr.com/services/rest/?" + (params.map { case (k, v) => s"$k=$v" } mkString "&")
    wsClient.url(url).get()
      .map(r ⇒ decode[SearchPhotosResponse](r.body))
      .map {
        case Xor.Right(res) ⇒ Ok(LoadPhotosResponse(res.photos.photo).asJson)
        case Xor.Left(error) ⇒ InternalServerError(error.getMessage)
      }
  }
}
