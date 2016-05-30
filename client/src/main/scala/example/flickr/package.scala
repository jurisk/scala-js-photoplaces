package example

package object flickr {
  case class Photo(
    latitude: Double,
    longitude: Double,
    farm: Int,
    server: String,
    id: String,
    secret: String
  ) {
    def thumbnail: String = url("t")

    private def url(mod: String): String = {
      s"https://farm$farm.staticflickr.com/$server/${id}_${secret}_$mod.jpg"
    }
  }

  case class SearchPhotosResponse(
    photos: SearchPhotosResponseDetails
  )

  case class SearchPhotosResponseDetails(
    photo: List[Photo]
  )
}
