package io.kipp.site

import io.circe._
import io.circe.generic.auto._

final case class Talk(
    title: String,
    slides: String,
    video: Option[String],
    place: Place
)

final case class Place(name: String, link: String)
