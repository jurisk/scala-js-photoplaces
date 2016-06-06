package net.photoplaces.services

import cats.data.Xor
import net.photoplaces.protocol.{LoadPhotosRequest, LoadPhotosResponse}
import net.photoplaces.protocol.Photo
import org.scalajs.dom.Coordinates
import org.scalajs.dom.ext.Ajax
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import japgolly.scalajs.react.extra.router.BaseUrl
import scala.concurrent.{ExecutionContext, Future}

class PhotoService {
  val baseUrl = BaseUrl.fromWindowOrigin_/

  def searchByLocation(coordinates: Coordinates)(implicit ec: ExecutionContext): Future[List[Photo]] = {
    Ajax.post(
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
}
