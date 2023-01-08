package io.kipp.site

enum ListItem:
  case Album(
      artist: String,
      album: String,
      link: String,
      `favorite-song`: String,
      rating: Int
  )

  case Article(title: String, author: String, link: String)

  case Site(url: String, owner: String, topics: Set[String])

  case Talk(
      title: String,
      slides: String,
      video: Option[String],
      place: Place
  )

  case Video(title: String, author: String, link: String)

final case class Place(name: String, link: String)
