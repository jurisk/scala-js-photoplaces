package example.flickr

import cats.data.Xor
import org.scalajs.dom
import org.scalajs.dom.Coordinates
import org.scalajs.dom.ext.Ajax
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import scala.concurrent.Future

class Service(apiKey: String) {
  def searchPhotos(coordinates: Coordinates): Future[List[Photo]] = {
    val params = Map(
      "method" -> "flickr.photos.search",
      "api_key" -> apiKey,
      "lat" -> coordinates.latitude.toString,
      "lon" -> coordinates.longitude.toString,
      "format" -> "json",
      "nojsoncallback" -> "1",
      "extras" -> "geo"
    )

    // TODO: paginate?
    val url = s"https://api.flickr.com/services/rest/?" + (params.map { case (k, v) => s"$k=$v" } mkString "&")
    dom.console.info(s"Invoking $url")
    import scala.concurrent.ExecutionContext.Implicits.global
    Ajax.get(url).map { x =>
      decode[SearchPhotosResponse](x.responseText) match {
        case Xor.Right(response) => {
          dom.console.info(s"Got ${response.photos.photo.length} results")
          response.photos.photo
        }

        case Xor.Left(error) => sys.error(s"Failed to parse ${x.responseText}: $error")
      }
    }
  }
}
