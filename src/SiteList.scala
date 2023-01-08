package io.kipp.site

import scalatags.Text.all.*
import scalatags.Text.tags2

sealed trait SiteList[A <: ListItem]:
  def title: String
  def description: String
  def items: Set[A] | Set[Map[String, Set[A]]]

object SiteList:
  import io.circe.Json
  import io.circe.yaml.parser
  import io.circe.generic.auto.*

  def fromPath(
      path: os.Path
  ): Either[String, Albums | Articles | Sites | Talks | Videos] =
    val contents = os.read(path)
    val json = parser.parse(contents)

    json match
      case Left(err) =>
        scribe.error(s"Choked when processing ${path}")
        Left(err.getMessage)
      case Right(json) =>
        toListOf(json) match
          case Left(err) =>
            scribe.error(s"Choked when processing ${path}")
            Left(err)
          case Right(value) => Right(value)

  private def toListOf(
      json: Json
  ): Either[String, Albums | Articles | Sites | Talks | Videos] =
    val cursor = json.hcursor
    val title = cursor
      .downField("title")
      .as[String]

    // TODO is there a better way to do this?
    // and when I do that get rid of the `-Xmax-inlines`
    title match
      case Right("albums")   => json.as[Albums].left.map(_.message)
      case Right("articles") => json.as[Articles].left.map(_.message)
      case Right("sites")    => json.as[Sites].left.map(_.message)
      case Right("talks")    => json.as[Talks].left.map(_.message)
      case Right("videos")   => json.as[Videos].left.map(_.message)
      case Right(value) => Left(s"${value} seems to be unmapped for decoded.")
      case Left(err)    => Left(err.getMessage)

final case class Albums(
    title: String,
    description: String,
    items: Set[ListItem.Album]
) extends SiteList[ListItem.Album]

final case class Articles(
    title: String,
    description: String,
    items: Set[Map[String, Set[ListItem.Article]]]
) extends SiteList[ListItem.Article]

final case class Sites(
    title: String,
    description: String,
    items: Set[ListItem.Site]
) extends SiteList[ListItem.Site]

final case class Talks(
    title: String,
    description: String,
    items: Set[ListItem.Talk]
) extends SiteList[ListItem.Talk]:
  def listHtml() =
    items.toSeq.map { talk =>
      div(
        p(talk.title),
        span(
          a(
            borderBottomStyle.none,
            href := talk.place.link,
            target := "_blank",
            talk.place.name
          ),
          " | ",
          a(
            borderBottomStyle.none,
            href := s"slides/${talk.slides}",
            target := "_blank",
            "slides"
          ),
          talk.video
            .map[scalatags.Text.Modifier] { vid =>
              Seq(
                stringFrag(" | "),
                a(
                  borderBottomStyle.none,
                  rel := "me noopener noreferrer",
                  target := "_blank",
                  href := vid,
                  "video"
                )
              )
            }
            .getOrElse(Seq.empty[scalatags.Text.Modifier])
        )
      )
    }

final case class Videos(
    title: String,
    description: String,
    items: Set[Map[String, Set[ListItem.Video]]]
) extends SiteList[ListItem.Video]
