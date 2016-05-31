package controllers

import example.protocol.{LoadPhotosRequest, LoadPhotosResponse, Photo}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

class Application(wsClient: WSClient, configuration: Configuration)(implicit environment: Environment) extends Controller {

  val flickrKey = configuration.getString("flickr-key").getOrElse(sys.error("flickr-key not found in config"))

  case class SearchPhotosResponse(photos: SearchPhotosResponseDetails)
  case class SearchPhotosResponseDetails(photo: List[Photo])

  def index(path: String) = Action {
    Ok(views.html.index("Title"))
  }

  implicit val format0 = Json.format[Photo]
  implicit val format1 = Json.format[LoadPhotosRequest]
  implicit val format2 = Json.format[LoadPhotosResponse]
  implicit val format3 = Json.format[SearchPhotosResponseDetails]
  implicit val format4 = Json.format[SearchPhotosResponse]

  def loadPhotos = Action.async(parse.json[LoadPhotosRequest]) { req ⇒
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
      .map(_
        .json
        .as[SearchPhotosResponse])
      .map(r ⇒
        Ok(Json.toJson(LoadPhotosResponse(r.photos.photo)))
      )
  }
}
