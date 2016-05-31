package net.photoplaces.components

import google.map.{GoogleMap, GoogleMapMarker}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactComponentU, TopNode}
import net.photoplaces.pages.Page
import net.photoplaces.protocol.Photo
import net.photoplaces.styles.GlobalStyles._

import scala.scalajs.js
import scalacss.ScalaCssReact._

object Map {
  val o = js.Dynamic.literal

  case class Props(photos: List[Photo], onPhotoClick: Photo ⇒ Unit, router: RouterCtl[Page])

  private val component = ReactComponentB[Props]("Map")
    .render_P { x =>
      div(mapStyle,
        ""
      )
    }
    .componentDidMount(scope ⇒ Callback {
      val photos = scope.props.photos

      val map = new GoogleMap(
        scope.getDOMNode(), o(
          center = o(
            lat = photos.map(_.latitude.toDouble).sum / photos.size,
            lng = photos.map(_.longitude.toDouble).sum / photos.size
          ), zoom = 12
        ))
      photos.foreach(photo ⇒ {
        val marker = new GoogleMapMarker(o(
          position = o(lat = photo.latitude.toDouble, lng = photo.longitude.toDouble),
          map = map,
          icon = photo.thumbnail
        ))
        marker.addListener("click", (e: js.Any) ⇒ scope.props.onPhotoClick(photo))
      })
      scope.forceUpdate.runNow()
    })
    .build

  def apply(photos: List[Photo], onMarkerClick: Photo ⇒ Unit, router: RouterCtl[Page]): ReactComponentU[Props, Unit, Unit, TopNode] = {
    component(Props(photos, onMarkerClick, router))
  }
}
