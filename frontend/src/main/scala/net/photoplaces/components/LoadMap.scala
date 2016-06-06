package net.photoplaces.components

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import net.photoplaces.pages.Page
import org.scalajs.dom.Coordinates

object LoadMap {
  case class Props(coordinates: Coordinates, ctx: RouterCtl[Page])

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props) =
      div()
  }
  private val component = ReactComponentB[Props]("LoadMap")
    .renderBackend[Backend]
    .build

  def apply(coords: Coordinates, ctx: RouterCtl[Page]) = component(Props(coords, ctx))
}
