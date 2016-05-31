package net.photoplaces.pages

sealed trait Page

object Page {
  case object Home extends Page
  case object Map extends Page
  case class FlickrPhoto(id: String, farm: Int, server: String, secret: String) extends Page {
    def url = s"https://farm$farm.staticflickr.com/$server/${id}_$secret.jpg"
  }
}
