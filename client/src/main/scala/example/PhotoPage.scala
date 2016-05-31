package example

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object PhotoPage {

  val component = ReactComponentB[Page.FlickrPhoto]("Photo page")
    .render_P(p ⇒
      <.div(
        <.h4(s"Photo from Flickr:"),
        <.img(^.src := p.url)
      )
    )
    .build

  def apply(p: Page.FlickrPhoto) = component(p)
}
