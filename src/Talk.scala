package io.kipp.site

import org.virtuslab.yaml.YamlCodec
import org.virtuslab.yaml.YamlEncoder
import org.virtuslab.yaml.Node
import org.virtuslab.yaml.Tag

final case class Talk(title: String, slides: String, video: Option[String])
    derives YamlCodec

object Talk:
  given [T](using encoder: YamlEncoder[T]): YamlEncoder[Option[T]] = {
    case Some(t) => encoder.asNode(t)
    case None    => Node.ScalarNode("!!null")
  }
