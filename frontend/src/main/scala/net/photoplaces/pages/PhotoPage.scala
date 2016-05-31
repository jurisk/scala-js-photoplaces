package net.photoplaces.pages

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.all._

object PhotoPage {

  val component = ReactComponentB[Page.FlickrPhoto]("Photo page")
    .render_P(p ⇒
      div(
        h4(s"Photo"),
        img(src := p.url)
      )
    )
    .build

  def apply(p: Page.FlickrPhoto) = component(p)
}
