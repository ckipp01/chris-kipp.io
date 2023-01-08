package io.kipp.site

import io.circe.*
import io.circe.generic.auto.*

final case class Talk(
    title: String,
    slides: String,
    video: Option[String],
    place: Place
)

final case class Place(name: String, link: String)
