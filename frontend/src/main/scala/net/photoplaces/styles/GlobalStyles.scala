package net.photoplaces.styles

import scala.language.postfixOps
import scalacss.mutable.StyleSheet
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {

  import dsl._

  style(unsafeRoot("body")(
    backgroundColor(lemonchiffon)
  ))

  val mapStyle = style(
    width(500 px),
    height(500 px)
  )
}
