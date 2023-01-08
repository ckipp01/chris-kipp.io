package io.kipp.site

final case class Album(
    artist: String,
    album: String,
    link: String,
    `favorite-song`: String,
    rating: Int
)
