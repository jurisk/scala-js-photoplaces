package net.photoplaces

import net.photoplaces.pages.{PhotoPage, Page, HomePage}
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfigDsl}

import scala.scalajs.js
import org.scalajs.dom
import japgolly.scalajs.react._

object Main extends js.JSApp {
  def main(): Unit = {
    val baseUrl = BaseUrl.fromWindowOrigin

    val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
      import dsl._

      val photoId = string("[a-z0-9_]+")

      (removeTrailingSlashes
        | staticRoute(root, Page.Home) ~> renderR(ctl => HomePage.component(ctl))
        | dynamicRouteCT[Page.FlickrPhoto](root / "photo" / photoId.caseClass[Page.FlickrPhoto]) ~> dynRender(PhotoPage(_)))
        .notFound { x =>
          dom.console.error(s"Page not found: $x")
          redirectToPage(Page.Home)(Redirect.Replace)
        }
    }

    val router = Router(baseUrl, routerConfig)
    val mountNode = dom.document.getElementById("mountNode")
    router() render mountNode
  }
}
