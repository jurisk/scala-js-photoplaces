package net.photoplaces.pages

sealed trait Page

object Page {
  case object Home extends Page
  case object Map extends Page
  case class FlickrPhoto(id: String) extends Page
}
