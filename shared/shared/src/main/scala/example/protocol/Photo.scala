package example.protocol

case class Photo(latitude: String,
                 longitude: String,
                 farm: Int,
                 server: String,
                 id: String,
                 secret: String) {
  def thumbnail: String = url("t")

  private def url(mod: String): String = {
    s"https://farm$farm.staticflickr.com/$server/${id}_${secret}_$mod.jpg"
  }
}
