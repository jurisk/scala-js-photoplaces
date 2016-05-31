package example.flickr

import cats.data.Xor
import example.protocol.{LoadPhotosRequest, LoadPhotosResponse, Photo}
import org.scalajs.dom
import org.scalajs.dom.Coordinates
import org.scalajs.dom.ext.Ajax
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import japgolly.scalajs.react.extra.router.BaseUrl
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

class Service {

  val baseUrl = BaseUrl.fromWindowOrigin_/

  def searchPhotos(coordinates: Coordinates): Future[List[Photo]] = Ajax.post(
    (baseUrl / "photos").value,
    LoadPhotosRequest(coordinates.latitude, coordinates.longitude).asJson.toString(),
    headers = Map("Content-Type" -> "application/json")
  )
    .map(xhr ⇒ decode[LoadPhotosResponse](xhr.responseText))
    .flatMap {
      case Xor.Right(response) ⇒ Future.successful(response.photos)
      case Xor.Left(error) ⇒ Future.failed(error)
    }

}
