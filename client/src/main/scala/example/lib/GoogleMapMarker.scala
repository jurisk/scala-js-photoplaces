package example.lib

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
@JSName("google.maps.Marker")
class GoogleMapMarker(options: js.Dynamic) extends js.Object {
  def addListener(event: String, callback: js.Function1[js.Any, Unit]): Unit = js.native
}
