package io.kipp.site

sealed trait ListItem

case class Album(
    artist: String,
    album: String,
    link: String,
    `favorite-song`: Option[String],
    rating: Int
) extends ListItem:
  def albumImageName =
    val name = album
      .replace(" ", "-")
      .replace("'", "")
      .replace(",", "")
      .replace(";", "")
      .replace("(", "")
      .replace(")", "")
      .replace("ş", "s")
      .toLowerCase() + ".jpeg"
    assert(
      os.isFile(os.pwd / "site" / "images" / "albums" / name),
      s"can't find album, ${name} -- make sure it exists and that you're running scala-cli from the root"
    )
    name

case class Article(title: String, author: String, link: String) extends ListItem

case class Site(url: String, owner: String, topics: Set[String])
    extends ListItem

case class Talk(
    title: String,
    slides: String,
    video: Option[String],
    place: Place
) extends ListItem

case class Video(title: String, author: String, link: String) extends ListItem

final case class Place(name: String, link: String) extends ListItem
