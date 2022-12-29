package io.kipp.site

import org.virtuslab.yaml.YamlCodec
import org.virtuslab.yaml.YamlEncoder
import org.virtuslab.yaml.Node
import org.virtuslab.yaml.Tag
import Encoders.given_YamlEncoder_Option

final case class Talk(
    title: String,
    slides: String,
    video: Option[String],
    place: Place
) derives YamlCodec

final case class Place(name: String, link: String) derives YamlCodec

object Encoders:
  given [T](using encoder: YamlEncoder[T]): YamlEncoder[Option[T]] = {
    case Some(t) => encoder.asNode(t)
    case None    => Node.ScalarNode("!!null")
  }
