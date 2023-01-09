package io.kipp.site

sealed trait ListItem

case class Album(
    artist: String,
    album: String,
    link: String,
    `favorite-song`: String,
    rating: Int
) extends ListItem

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
