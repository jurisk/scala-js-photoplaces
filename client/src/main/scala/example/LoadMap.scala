package example

import example.flickr.{Service, Photo}
import example.model.Marker
import japgolly.scalajs.react.{Callback, BackendScope, ReactComponentB}
import japgolly.scalajs.react.extra.router.RouterCtl
import org.scalajs.dom
import org.scalajs.dom.Coordinates
import japgolly.scalajs.react.vdom.prefix_<^._

object LoadMap {
  case class Props(coordinates: Coordinates, ctx: RouterCtl[Page])
  case class State(photos: List[Photo] = Nil)

  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = {
      <.div(
        if (state.photos.nonEmpty) {
          val markers = state.photos.map { photo =>
            Marker(photo)
          }

          dom.console.info(s"${markers.length} markers")

          Map(markers,
            m ⇒ props.ctx.set(Page.FlickrPhoto(m.photo.id, m.photo.farm, m.photo.server, m.photo.secret)).runNow(),
            props.ctx)
        } else EmptyTag
      )
    }
  }

  private val component = ReactComponentB[Props]("LoadMap")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount { scope =>
      val coords = scope.props.coordinates
      import scala.concurrent.ExecutionContext.Implicits.global
      val flickrApiKey = Globals.flickrKey
      val f = new Service(flickrApiKey).searchPhotos(coords).map { results =>
        scope.modState(_.copy(photos = results))
      }
      Callback.future(f)
    }
    // TODO - what about update Props?
    .build

  def apply(coords: Coordinates, ctx: RouterCtl[Page]) = component(Props(coords, ctx))
}
