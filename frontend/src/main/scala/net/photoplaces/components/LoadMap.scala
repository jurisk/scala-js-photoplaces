package net.photoplaces.components

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import net.photoplaces.services.PhotoService
import net.photoplaces.pages.Page
import net.photoplaces.protocol.Photo
import org.scalajs.dom
import org.scalajs.dom.Coordinates

object LoadMap {
  case class Props(coordinates: Coordinates, ctx: RouterCtl[Page])
  case class State(photos: List[Photo] = Nil)

  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = {
      div(
        if (state.photos.nonEmpty) {
          dom.console.info(s"${state.photos.length} photos")

          Map(
            state.photos,
            photo ⇒ props.ctx.set(Page.FlickrPhoto(photo.id, photo.farm, photo.server, photo.secret)).runNow(),
            props.ctx
          )
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
      val f = new PhotoService().searchByLocation(coords).map { results =>
        scope.modState(_.copy(photos = results))
      }
      Callback.future(f)
    }
    .build

  def apply(coords: Coordinates, ctx: RouterCtl[Page]) = component(Props(coords, ctx))
}
