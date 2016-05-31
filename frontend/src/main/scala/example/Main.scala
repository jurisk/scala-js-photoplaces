package example

import example.styles.GlobalStyles
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfigDsl}

import scala.scalajs.js
import org.scalajs.dom
import japgolly.scalajs.react._

import scala.util.Try
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object Main extends js.JSApp {
  def main(): Unit = {
    val baseUrl = BaseUrl.fromWindowOrigin

    val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
      import dsl._

      val photoId = string("[a-z0-9_]+")

      (trimSlashes
        | staticRoute(root, Page.Home) ~> renderR(ctl => HomePage.component(ctl))
        | dynamicRouteCT[Page.FlickrPhoto](("#photo" / photoId)
        .pmap(_.split("_").toList match {
          case id :: farm :: server :: secret :: Nil ⇒
            Try(farm.toInt).toOption.map(f ⇒
              Page.FlickrPhoto(id, f, server, secret)
            )
          case other ⇒ None
        })(fp ⇒ s"${fp.id}_${fp.farm}_${fp.server}_${fp.secret}")) ~> dynRender(PhotoPage(_))
        )
        .notFound { x =>
          dom.console.error(s"Page not found: $x")
          redirectToPage(Page.Home)(Redirect.Replace)
        }
        .onPostRender { (prev, cur) =>
          Callback.log(s"$Page changing from $prev to $cur.")
        }
    }

    val router = Router(baseUrl, routerConfig)
    val mountNode = dom.document.getElementById("mountNode")
    GlobalStyles.addToDocument
    router() render mountNode
  }
}
