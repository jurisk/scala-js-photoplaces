package net.photoplaces.pages

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import net.photoplaces.components.LoadMap
import org.scalajs.dom
import org.scalajs.dom.raw.{Position, PositionError}

object HomePage {
  case class State(
    position: Option[Position] = None
  )

  type Props = RouterCtl[Page]

  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = {
      div(
        state.position.map { position =>
          div(
            s"Location: ${position.coords.latitude}, ${position.coords.longitude}",
            LoadMap(position.coords, props)
          )
        }.getOrElse {
          div(s"Detecting location...")
        }
      )
    }
  }

  val component = ReactComponentB[Props]("HomePage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount { scope => Callback {
        dom.window.navigator.geolocation.getCurrentPosition(
          (pos: Position) => {
            scope.modState(_.copy(position = Some(pos))).runNow()
          },
          (error: PositionError) => {
            println(s"Error getting geo data $error")
          }
        )
      }
    }
    .build
}
