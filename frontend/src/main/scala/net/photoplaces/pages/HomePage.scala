package net.photoplaces.pages

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

object HomePage {

  type Props = RouterCtl[Page]

  class Backend($ : BackendScope[Props, Unit]) {
    def render(props: Props) = {
      div(
          div(s"Detecting location...")
      )
    }
  }

  val component =
    ReactComponentB[Props]("HomePage").renderBackend[Backend].build
}
