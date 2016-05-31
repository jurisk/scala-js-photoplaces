package example

import example.lib.{GoogleMap, GoogleMapMarker}
import example.model.Marker
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{TopNode, ReactComponentU, Callback, ReactComponentB}
import example.styles.GlobalStyles._
import scala.scalajs.js
import scalacss.ScalaCssReact._

object Map {
  val o = js.Dynamic.literal

  case class Props(markers: List[Marker], onMarkerClick: Marker ⇒ Unit, router: RouterCtl[Page])

  private val component = ReactComponentB[Props]("Map")
    .render_P { x =>
      <.div(mapStyle,
        ""
      )
    }
    .componentDidMount(scope ⇒ Callback {
      val markers = scope.props.markers

      val map = new GoogleMap(
        scope.getDOMNode(), o(
          center = o(
            lat = markers.map(_.photo.latitude.toDouble).sum / markers.size,
            lng = markers.map(_.photo.longitude.toDouble).sum / markers.size
          ), zoom = 12
        ))
      markers.foreach(m ⇒ {
        val marker = new GoogleMapMarker(o(
          position = o(lat = m.photo.latitude.toDouble, lng = m.photo.longitude.toDouble),
          map = map,
          icon = m.photo.thumbnail
        ))
        marker.addListener("click", (e: js.Any) ⇒ scope.props.onMarkerClick(m))
      })
      scope.forceUpdate.runNow()
    })
    .build

  def apply(markers: List[Marker], onMarkerClick: Marker ⇒ Unit, router: RouterCtl[Page]): ReactComponentU[Props, Unit, Unit, TopNode] = {
    component(Props(markers, onMarkerClick, router))
  }
}
